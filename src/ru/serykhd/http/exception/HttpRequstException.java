package ru.serykhd.http.exception;

public class HttpRequstException extends RuntimeException {

    public HttpRequstException(String message) {
        super(message);
    }

    public HttpRequstException(String message, Throwable throwable) {
        super(message, throwable);
    }
}