package ru.serykhd.http.response;

import io.netty.handler.codec.http.HttpResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.ToString.Exclude;

@RequiredArgsConstructor
@Getter
@ToString
public class WHttpRequstResponse {

	private final HttpResponse response;
	private final StringBuilder content;
	@Exclude
	private final byte[] data;
	private final ResponseStatistics responseStatistics;
}
