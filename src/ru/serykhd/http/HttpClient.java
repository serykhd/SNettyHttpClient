package ru.serykhd.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.resolver.dns.DefaultDnsServerAddressStreamProvider;
import io.netty.resolver.dns.DnsAddressResolverGroup;
import lombok.Getter;
import lombok.NonNull;
import ru.serykhd.common.service.ShutdownableService;
import ru.serykhd.common.thread.SThreadFactory;
import ru.serykhd.http.callback.HttpCallback;
import ru.serykhd.http.handler.HttpHandler;
import ru.serykhd.http.initializer.ConnectionConstants;
import ru.serykhd.http.initializer.HttpClientInitializer;
import ru.serykhd.http.keepalive.KeepAliveStrategy;
import ru.serykhd.http.proxy.impl.pool.ProxyPool;
import ru.serykhd.http.requst.WHttpRequest;
import ru.serykhd.http.requst.WHttpRequestBuilder;
import ru.serykhd.http.response.WHttpRequstResponse;
import ru.serykhd.logger.InternalLogger;
import ru.serykhd.logger.impl.InternalLoggerFactory;
import ru.serykhd.logger.level.Level;
import ru.serykhd.netty.transport.TransportUtils;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Getter
public class HttpClient implements ShutdownableService {


	private static final DnsAddressResolverGroup dnsResolverGroup = new DnsAddressResolverGroup(TransportUtils.bestType().getDatagramChannelFactory(), DefaultDnsServerAddressStreamProvider.INSTANCE);

	private final InternalLogger logger = InternalLoggerFactory.getInstance(HttpClient.class).setLevel(Level.INFO);

	@Deprecated
	private final EventLoopGroup group;

	private long connectTimeout = 250;
	private long readTimeout = 3_000;

	private KeepAliveStrategy keepAliveStrategy = new KeepAliveStrategy();

	private ProxyPool proxyPool;

	public HttpClient() {
		this(new SThreadFactory("HttpClient Group"));
	}

	public HttpClient(ThreadFactory threadFactory) {
		this(TransportUtils.bestType().newEventLoopGroup(1, threadFactory));
	}

	public HttpClient(@NonNull EventLoopGroup group) {
		this.group = group;
	}

	// если используется прокси
	public HttpClient connectTimeout(int timeout, @NonNull TimeUnit unit) {
		this.connectTimeout = unit.toMillis(timeout);
		return this;
	}

	public HttpClient readTimeout(int timeout, @NonNull TimeUnit unit) {
		this.readTimeout = unit.toMillis(timeout);
		return this;
	}

	public HttpClient proxyPool(@NonNull ProxyPool proxyPool) {
		this.proxyPool = proxyPool;
		return this;
	}

	public WHttpRequestBuilder create(@NonNull String url, @NonNull HttpCallback<WHttpRequstResponse> callback) {
		URI uri = URI.create(url);

		Objects.requireNonNull(uri.getScheme());
		Objects.requireNonNull(uri.getHost());

		int port = uri.getPort();
		if (port == -1) {
			switch (uri.getScheme()) {
				case "http":
					port = 80;
					break;
				case "https":
					port = 443;
					break;
				default:
					throw new IllegalArgumentException("Unknown scheme " + uri.getScheme());
			}
		}

		return new WHttpRequestBuilder(this, uri, port, callback);
	}

	public ChannelFuture execute(@NonNull WHttpRequest request) {

		// reusing channel
		ChannelHandlerContext ctx = keepAliveStrategy.getConnection(request.getRequstBuilder());
		if (ctx != null) {

			ctx.writeAndFlush(request.getRequest(), ctx.voidPromise());

			ChannelPipeline p = ctx.pipeline();

			p.replace(ConnectionConstants.HANDLER, ConnectionConstants.HANDLER, new HttpHandler(request));

			// return ctx.writeAndFlush(request.getRequest(), ctx.voidPromise()); ???
			return null;
		}

		//
		ChannelFutureListener listener = (future) -> {
			if (future.isSuccess()) {
				Channel ch = future.channel();

				ch.writeAndFlush(request.getRequest(), ch.voidPromise());

				return;
			}

			future.channel().close();

			if (future.channel().hasAttr(ConnectionConstants.PROXY_ATTRIBUTE_KEY)) {
				if (request.getRetry() <= 3) {
					request.setRetry(request.getRetry() + 1);
					//System.out.println("retrying req with proxy .... " + future.cause().getMessage());
					execute(request);
					return;

				}
			}

			request.getRequstBuilder().getCallback().cause(future.cause());
		};

		Bootstrap bootstrap = new Bootstrap()
				.channelFactory(TransportUtils.bestType().getSocketChannelFactory())
				.group(group)
				.handler(new HttpClientInitializer(request))
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) request.getRequstBuilder().getConnectTimeout())
				.remoteAddress(request.getRequstBuilder().getUri().getHost(), request.getRequstBuilder().getPort())
				.resolver(dnsResolverGroup);

		return bootstrap.connect().addListener(listener);
	}

	@Override
	public void shutdown() {
		logger.info("Closing IO threads ...");

		group.shutdownGracefully();

		while (true) {
			try {
				group.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
				break;
			} catch (InterruptedException ignored) {
			}
		}

		logger.info("Closed IO threads!");
	}
}
