package ru.serykhd.http.mime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MimeType {

	JSON("application/json");
	
	private final String value;
}
