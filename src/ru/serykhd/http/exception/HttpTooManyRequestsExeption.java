package ru.serykhd.http.exception;

public class HttpTooManyRequestsExeption extends HttpRequstException {

    public HttpTooManyRequestsExeption() {
        super("Too Many Requests");
    }
}