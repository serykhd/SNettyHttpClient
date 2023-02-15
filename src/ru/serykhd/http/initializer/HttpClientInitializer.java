package ru.serykhd.http.initializer;

import ru.serykhd.http.handler.HttpHandler;
import ru.serykhd.http.proxy.impl.Proxy;
import ru.serykhd.http.requst.WHttpRequest;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class HttpClientInitializer extends ChannelInitializer<SocketChannel> {
	
    private final WHttpRequest request;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        // proxy
        if (request.getRequstBuilder().getProxyPool() != null) {
            Proxy proxy = request.getRequstBuilder().getProxyPool().getRandomProxy();

            ch.attr(ConnectionConstants.PROXY_ATTRIBUTE_KEY).set(proxy);

            ProxyHandler proxyHandler = proxy.createProxyHandler();

            // время коннекта от прокси сервера до адрреса куда будут идти реквесты
            proxyHandler.setConnectTimeoutMillis(request.getRequstBuilder().getProxyPool().getConnectTimeout());

            p.addFirst(proxyHandler);
        }

        p.addLast(ConnectionConstants.READ_TIMEOUT, new ReadTimeoutHandler(request.getRequstBuilder().getReadTimeout(), TimeUnit.MILLISECONDS));

        if (request.getRequstBuilder().isSsl()) {
            SslContext context = SslContextBuilder.forClient().build();
            SSLEngine engine = context.newEngine(ch.alloc(), request.getRequstBuilder().getUri().getHost(), request.getRequstBuilder().getPort());

            p.addLast(ConnectionConstants.SSL_HANDLER, new SslHandler(engine));
        }

        p.addLast(ConnectionConstants.HTTP_CODEC, new HttpClientCodec());
        p.addLast(ConnectionConstants.HTTP_DECOMPRESSOR, new HttpContentDecompressor());
        p.addLast(ConnectionConstants.HANDLER, new HttpHandler(request));
    }
}