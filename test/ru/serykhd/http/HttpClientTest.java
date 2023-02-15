package ru.serykhd.http;

import ru.serykhd.http.callback.HttpCallback;
import ru.serykhd.http.proxy.ProxyType;
import ru.serykhd.http.proxy.impl.Proxy;
import ru.serykhd.http.proxy.impl.pool.ProxyPool;
import ru.serykhd.http.requst.WHttpRequestBuilder;
import ru.serykhd.http.response.WHttpRequstResponse;
import ru.serykhd.http.utils.RequstResponseUtils;
import ru.serykhd.netty.transport.TransportType;
import ru.serykhd.netty.transport.TransportUtils;

import java.util.concurrent.TimeUnit;

public class HttpClientTest {

    public static void main(String[] args) throws Exception {
        TransportUtils.bestType = TransportType.EPOLL;

        HttpClient cl = new HttpClient();

        ProxyPool pool = new ProxyPool();

        pool.addProxy(new Proxy(ProxyType.SOCKS5,"72.195.114.184", 4145));
        cl.proxyPool(pool);

        // http://ip-api.com/json/24.48.0.1
        WHttpRequestBuilder builder = cl.create("http://ip-api.com/json/24.48.0.1", new HttpCallback<WHttpRequstResponse>() {

            @Override
            public void done(WHttpRequstResponse result) {
                RequstResponseUtils.checkOk(result, this);

                System.out.println(result);
            }

            @Override
            public void cause(Throwable cause) {
                //System.err.println(cause.getMessage());
                cause.printStackTrace();
                // TODO Auto-generated method stub

            }
        });

        // коннект до прокси
        builder.connectTimeout(250, TimeUnit.MILLISECONDS);

        // после врайта время ожидания
        builder.readTimeout(250, TimeUnit.MILLISECONDS);
        builder.readTimeout(10000, TimeUnit.MILLISECONDS);


        cl.execute(builder.createRequst());

        Thread.sleep(1000);

		/*
		java.util.Collection<java.lang.StackTraceElement[]> a1 = java.lang.Thread.getAllStackTraces().values();
		for (java.lang.StackTraceElement[] a2 : a1){
			System.out.println("==========");
			for (java.lang.StackTraceElement a3 : a2){
				System.out.println(a3.toString());
			}
		}

		 */
    }
}