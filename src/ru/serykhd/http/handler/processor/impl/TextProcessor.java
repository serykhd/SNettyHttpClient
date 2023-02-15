package ru.serykhd.http.handler.processor.impl;

import ru.serykhd.http.handler.processor.IHttpContentProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.util.CharsetUtil;

public class TextProcessor implements IHttpContentProcessor<StringBuilder> {

    private StringBuilder builder;

    @Override
    public void process(HttpContent content) {
        if (builder == null) {
            builder = new StringBuilder();
        }

        ByteBuf buf = content.content();

        //
        builder.append(buf.toString(CharsetUtil.UTF_8));
    }

    @Override
    public StringBuilder result() {
        return builder;
    }
}
