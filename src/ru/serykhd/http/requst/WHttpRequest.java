package ru.serykhd.http.requst;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
public class WHttpRequest {

	private final WHttpRequestBuilder requstBuilder;
	private final DefaultFullHttpRequest request;

	@Setter
	private int retry;
	
	public WHttpRequest(WHttpRequestBuilder requstBuilder, DefaultFullHttpRequest request) {
		this.requstBuilder = requstBuilder;
		this.request = request;
	}
}
