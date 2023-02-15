package ru.serykhd.http.handler;

import ru.serykhd.http.exception.HttpBreakException;
import ru.serykhd.http.handler.processor.IHttpContentProcessor;
import ru.serykhd.http.handler.processor.impl.BytesProcessor;
import ru.serykhd.http.handler.processor.impl.TextProcessor;
import ru.serykhd.http.initializer.ConnectionConstants;
import ru.serykhd.http.requst.WHttpRequest;
import ru.serykhd.http.response.ResponseStatistics;
import ru.serykhd.http.response.WHttpRequstResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Getter
    private final WHttpRequest request;

    private HttpResponse response;
    private final IHttpContentProcessor<?> processor;

    private final ResponseStatistics responseStatistics = new ResponseStatistics();

    public HttpHandler(WHttpRequest request) {
        this.request = request;

        // todo https://github.com/SpigotMC/BungeeCord/pull/3402
        switch (request.getRequstBuilder().getHandlerType()) {
            case BYTES:
                processor = new BytesProcessor();
                break;
            case TEXT:
                processor = new TextProcessor();
                break;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        responseStatistics.setInitialConnection();
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
            response = (HttpResponse) msg;

            responseStatistics.setWaitingFirstByte();
            return;
        }

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;

            processor.process(content);

            if (content instanceof LastHttpContent) {
                responseStatistics.setContentDownloading();

                request.getRequstBuilder().getClient().getKeepAliveStrategy().keepAliveOrClose(request, ctx, response);

                try {
                    switch (request.getRequstBuilder().getHandlerType()) {
                        case BYTES:
                            request.getRequstBuilder().getCallback().done(new WHttpRequstResponse(response, null, (byte[]) processor.result(), responseStatistics));
                            break;
                        case TEXT:
                            request.getRequstBuilder().getCallback().done(new WHttpRequstResponse(response, (StringBuilder) processor.result(), null, responseStatistics));
                            break;
                    }
                } catch (HttpBreakException e) {
                    // break
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            return;
        }

        throw new AssertionError();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        request.getRequstBuilder().getClient().getKeepAliveStrategy().keepAliveRemove(ctx, request.getRequstBuilder());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // как я понял, http умеет только 200 получать
        // io.netty.handler.proxy.ProxyHandler.handleResponse
        // http, none, /51.104.203.226:33300 => ip-api.com/208.95.112.1:80, status: 403 Forbidden
       // if (cause instanceof HttpProxyHandler.HttpProxyConnectException) {
       //     System.err.println("proxy 11111111111111111111111111111111111111111111111111111111111111111 exceptionCaught " + cause.getMessage());
       // }

      //  cause.printStackTrace();

        if (ctx.pipeline().get(ConnectionConstants.READ_TIMEOUT) == null) {
            return;
        }

        if (!ctx.channel().isActive()) {

            return;
        }

        // System.err.println("exceptionCaught " + ctx.channel().isActive() + " " + ctx.channel().isOpen());
        ctx.close();

        request.getRequstBuilder().getCallback().cause(cause);

    }
}