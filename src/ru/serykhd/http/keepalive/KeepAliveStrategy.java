package ru.serykhd.http.keepalive;

import ru.serykhd.http.requst.WHttpRequest;
import ru.serykhd.http.requst.WHttpRequestBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.timeout.ReadTimeoutHandler;
import ru.serykhd.logger.InternalLogger;
import ru.serykhd.logger.impl.InternalLoggerFactory;
import ru.serykhd.logger.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static ru.serykhd.http.initializer.ConnectionConstants.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;

public class KeepAliveStrategy {

    private final InternalLogger logger = InternalLoggerFactory.getInstance(KeepAliveStrategy.class).setLevel(Level.TRACE);

    private final Map<String, List<ChannelHandlerContext>> connections = new ConcurrentHashMap<>();

    public synchronized ChannelHandlerContext getConnection(WHttpRequestBuilder requestBuilder) {
        String key = getChannelKey(requestBuilder);

        List<ChannelHandlerContext> connected = connections.get(key);

        if (connected == null || connected.size() < 1) {
            return null;
        }

        ChannelHandlerContext ctx = connected.remove(0);

        // возвращаем хандел на тайм аут, ранее мы его сняли
        if (requestBuilder.isSsl()) {
            ctx.pipeline().addBefore(SSL_HANDLER, READ_TIMEOUT, new ReadTimeoutHandler(requestBuilder.getReadTimeout(), TimeUnit.MILLISECONDS));
        } else {
            ctx.pipeline().addBefore(HTTP_CODEC, READ_TIMEOUT, new ReadTimeoutHandler(requestBuilder.getReadTimeout(), TimeUnit.MILLISECONDS));
        }

        logger.debug("Reusing channel {} (key: {}, aviable {})", ctx.channel(), key, connected.size());

        return ctx;
    }

    public void keepAliveOrClose(WHttpRequest request, ChannelHandlerContext ctx, HttpResponse response) {
        if (!checkKeepAlive(request.getRequest(), response)) {
            ctx.close();
            return;
        }

        if (ctx.channel().hasAttr(PROXY_ATTRIBUTE_KEY)) {
            if (response.status() == HttpResponseStatus.OK) {
                keepAlive(ctx, request.getRequstBuilder());
            } else ctx.close();
        } else {
            keepAlive(ctx, request.getRequstBuilder());
        }
    }

    private synchronized void keepAlive(ChannelHandlerContext ctx, WHttpRequestBuilder requestBuilder) {
        String key = getChannelKey(requestBuilder);

        List<ChannelHandlerContext> connected = connections.computeIfAbsent(key, k -> new ArrayList<>());

        connected.add(ctx);

        ChannelPipeline pipe = ctx.pipeline();

        pipe.remove(READ_TIMEOUT);

        // TODO сервер сам закроет соединение, не будем делать извращения всяческие (ну будем надеяться, вообще да, может и не закрыть но пока пох)
        // p.addFirst(new ReadTimeoutHandler(Integer.valueOf(response.headers().get(KEEP_ALIVE).split("=")[1]), TimeUnit.SECONDS));

        logger.debug("Added activity channel {} (key: {}, aviable {})", ctx.channel(), key, connected.size());
    }

    public synchronized void keepAliveRemove(ChannelHandlerContext ctx, WHttpRequestBuilder requestBuilder) {
        String key = getChannelKey(requestBuilder);

        List<ChannelHandlerContext> connected = connections.get(key);

        if (connected == null) {
            return;
        }

        if (connected.remove(ctx)) {
            logger.debug("Remove channel {} (key: {}, aviable {})", ctx.channel(), key, connected.size());
        }
    }

    private static String getChannelKey(WHttpRequestBuilder requestBuilder) {
        String key = requestBuilder.getUri().getHost() + ":" + requestBuilder.getPort();

        if (requestBuilder.getProxyPool() != null) {
            key = "proxy://" + key;
        }

        return key;
    }

    private static boolean checkKeepAlive(HttpRequest request, HttpResponse response) {
        return HttpUtil.isKeepAlive(response)
                && HttpUtil.isKeepAlive(request)
                // support non standard Proxy-Connection
                && !response.headers().contains("Proxy-Connection", CLOSE, true);
    }
}
