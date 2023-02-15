package ru.serykhd.http.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestUtils {

    @Getter
    private final Gson gson = new GsonBuilder().create();
}
