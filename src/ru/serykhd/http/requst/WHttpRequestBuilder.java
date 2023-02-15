package ru.serykhd.http.requst;

import ru.serykhd.http.HttpClient;
import ru.serykhd.http.callback.HttpCallback;
import ru.serykhd.http.handler.HandlerType;
import ru.serykhd.http.mime.MimeType;
import ru.serykhd.http.proxy.impl.pool.ProxyPool;
import ru.serykhd.http.response.WHttpRequstResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.Getter;
import lombok.NonNull;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Getter
public class WHttpRequestBuilder {
	
	private final HttpClient client;
	private final URI uri;
	private final int port;
	private final HttpCallback<WHttpRequstResponse> callback;
	
	private long connectTimeout;
	private long readTimeout;
	
	private HttpVersion version = HttpVersion.HTTP_1_1;
	private HttpMethod method = HttpMethod.GET;
	
	private MimeType contentType;
	private MimeType acceptType;
	
	private HandlerType handlerType = HandlerType.TEXT;
	
	// Only post
	private byte[] bytes;
	
	private String path;
	private boolean ssl;

	private ProxyPool proxyPool;

	private boolean disableKeepAlive;
	
	public WHttpRequestBuilder(HttpClient client, URI uri, int port, HttpCallback<WHttpRequstResponse> callback) {
		this.client = client;
		this.uri = uri;
		this.port = port;
		this.callback = callback;
		
		this.connectTimeout = client.getConnectTimeout();
		this.readTimeout = client.getReadTimeout();
		
		//
		this.ssl = uri.getScheme().equals("https");
		
		this.path = uri.getRawPath();
		
		if (uri.getRawQuery() != null) {
			path += "?" + uri.getRawQuery();
		}

		this.proxyPool = client.getProxyPool();
	}

	public WHttpRequestBuilder ProxyPool(@NonNull ProxyPool proxyPool) {
		this.proxyPool = proxyPool;
		return this;
	}
	
	public WHttpRequestBuilder connectTimeout(int timeout, @NonNull TimeUnit unit) {
		this.connectTimeout = unit.toMillis(timeout);
		return this;
	}
	
	public WHttpRequestBuilder readTimeout(int timeout, @NonNull TimeUnit unit) {
		this.readTimeout = unit.toMillis(timeout);
		return this;
	}
	
	public WHttpRequestBuilder requestMethod(@NonNull HttpMethod method) {
		this.method = method;
		return this;
	}
	
	public WHttpRequestBuilder contentType(@NonNull MimeType type) {
		this.contentType = type;
		return this;
	}
	
	public WHttpRequestBuilder acceptType(@NonNull MimeType type) {
		this.acceptType = type;
		return this;
	}
	
	public WHttpRequestBuilder setBytes(byte[] bytes) {
		this.bytes = bytes;
		return this;
	}

	public WHttpRequestBuilder handlerType(HandlerType handler) {
		this.handlerType = handler;
		return this;
	}

	public WHttpRequestBuilder disableKeepAlive() {
		this.disableKeepAlive = true;
		return this;
	}
	
	public WHttpRequest createRequst() {
		DefaultFullHttpRequest request;
		
		//
		if (method == HttpMethod.POST && bytes != null) {
			request = new DefaultFullHttpRequest(version, method, path);
		}
		else request = new DefaultFullHttpRequest(version, method, path, Unpooled.EMPTY_BUFFER);
		
		// HOST
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());

		if (disableKeepAlive) {
			request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
		} else {
			// keep alive if needed
			if (version == HttpVersion.HTTP_1_0) {
				request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			}
		}



		// compression
		request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        
        // contentType if needed
        if (contentType != null) {
        	request.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType.getValue());
        }
        
        // acceptType if needed
        if (acceptType != null) {
        	request.headers().set(HttpHeaderNames.ACCEPT, acceptType.getValue());
        }
        
        //
        if (bytes != null) {
            ByteBuf buffer = request.content().clear();
            
            int p0 = buffer.writerIndex();
            
            buffer.writeBytes(bytes);
            
            int p1 = buffer.writerIndex();

			HttpUtil.setContentLength(request, p1 - p0);
        }

        /*
        request.headers().set(
                HttpHeaderNames.COOKIE,
                ClientCookieEncoder.STRICT.encode(
                        new DefaultCookie("my-cookie", "foo"),
                        new DefaultCookie("another-cookie", "bar")));*/
		return new WHttpRequest(this, request);
	}
}
