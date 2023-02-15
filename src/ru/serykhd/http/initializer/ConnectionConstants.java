package ru.serykhd.http.initializer;

import ru.serykhd.http.proxy.impl.Proxy;
import io.netty.util.AttributeKey;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConnectionConstants {

    public final String HANDLER = "handler";
    public final String SSL_HANDLER = "ssl-handler";
    public final String READ_TIMEOUT = "read-timeout";
    public final String HTTP_CODEC = "http-codec";

    public final String HTTP_DECOMPRESSOR = "http-decompressor";

    public final AttributeKey<Proxy> PROXY_ATTRIBUTE_KEY = AttributeKey.valueOf("proxy");
}
