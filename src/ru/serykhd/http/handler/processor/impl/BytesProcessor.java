package ru.serykhd.http.handler.processor.impl;

import ru.serykhd.http.handler.processor.IHttpContentProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;

import java.util.ArrayList;
import java.util.List;

public class BytesProcessor implements IHttpContentProcessor<byte[]> {

    private List<byte[]> datas;

    @Override
    public void process(HttpContent content) {
        if (datas == null) {
            datas = new ArrayList<>();
        }

        ByteBuf buf = content.content();

        //

        if (buf.isReadable()) {
            int contentLength = buf.readableBytes();

            byte[] data = new byte[contentLength];
            buf.readBytes(data);

            datas.add(data);
        }
    }

    @Override
    public byte[] result() {
        return concat();
    }

    private byte[] concat() {
        // Determine the length of the result array
        int totalLength = 0;
        for (byte[] data : datas) {
            totalLength += data.length;
        }

        // create the result array
        byte[] result = new byte[totalLength];

        // copy the source arrays into the result array
        int currentIndex = 0;
        for (byte[] data : datas) {
            System.arraycopy(data, 0, result, currentIndex, data.length);
            currentIndex += data.length;
        }

        return result;
    }
}
