package ru.serykhd.http.handler.processor;

import io.netty.handler.codec.http.HttpContent;

public interface IHttpContentProcessor<K> {

    void process(HttpContent content);

    K result();
}
