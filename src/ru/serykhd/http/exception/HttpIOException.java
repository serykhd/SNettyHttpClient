package ru.serykhd.http.exception;

public class HttpIOException extends HttpRequstException {

    public HttpIOException(String message) {
        super(String.format("I/O Exeption: %s", message));
    }

    @Override
    public Throwable initCause(Throwable cause) {
        return this;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
