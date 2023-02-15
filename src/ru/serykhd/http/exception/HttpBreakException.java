package ru.serykhd.http.exception;

public class HttpBreakException extends RuntimeException {

    public static HttpBreakException INSTANCE = new HttpBreakException();

}
