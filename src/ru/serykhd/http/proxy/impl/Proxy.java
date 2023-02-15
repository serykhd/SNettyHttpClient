package ru.serykhd.http.proxy.impl;

import ru.serykhd.http.proxy.ProxyType;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.function.Supplier;

@EqualsAndHashCode
@ToString
public class Proxy {

    @Getter
    private final ProxyType proxyType;
    @Getter
    private final SocketAddress socketAddress;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final Supplier<ProxyHandler> handler;

    public Proxy(ProxyType proxyType, SocketAddress socketAddress) {
        this.proxyType = proxyType;
        this.socketAddress = socketAddress;

        switch (proxyType) {
            case HTTP:
            case HTTPS: {
                this.handler = () -> new HttpProxyHandler(socketAddress);
                break;
            }
            case SOCKS4: {
                this.handler = () -> new Socks4ProxyHandler(socketAddress);
                break;
            }
            case SOCKS5: {
                this.handler = () -> new Socks5ProxyHandler(socketAddress);
                break;
            }
            default: {
                //throw new AssertionError();
                handler = null;
            }
        }
    }

    public Proxy(ProxyType proxyType, String inetHost, int inetPort) {
        this(proxyType, new InetSocketAddress(inetHost, inetPort));
    }

    public ProxyHandler createProxyHandler() {
        return handler.get();
    }
}
