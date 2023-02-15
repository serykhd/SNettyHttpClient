package ru.serykhd.http.utils;

import ru.serykhd.http.callback.HttpCallback;
import ru.serykhd.http.exception.HttpBreakException;
import ru.serykhd.http.exception.HttpIOException;
import ru.serykhd.http.exception.HttpTooManyRequestsExeption;
import ru.serykhd.http.response.WHttpRequstResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RequstResponseUtils {

    public void checkOk(@NonNull WHttpRequstResponse response, @NonNull HttpCallback callback) {
        if (response.getResponse().status() == HttpResponseStatus.TOO_MANY_REQUESTS) {
            callback.cause(new HttpTooManyRequestsExeption());
            throw HttpBreakException.INSTANCE;
        }

        if (response.getResponse().status() != HttpResponseStatus.OK) {
            callback.cause(new HttpIOException(String.format("bad status code: %s", response.getResponse().status())));
            throw HttpBreakException.INSTANCE;
        }
    }
}
