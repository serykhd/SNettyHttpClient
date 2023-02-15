package ru.serykhd.http.proxy.impl.pool;

import ru.serykhd.http.proxy.impl.Proxy;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ProxyPool {

    private List<Proxy> proxys = new ArrayList<>();
    @Getter
    private long connectTimeout = 5_000;

    public ProxyPool() {

    }

    public ProxyPool connectTimeout(int timeout, @NonNull TimeUnit unit) {
        this.connectTimeout = unit.toMillis(timeout);
        return this;
    }

    // check unique ??? TODO
    public void addProxy(@NonNull Proxy proxy) {
        proxys.add(proxy);
    }

    public void addAllProxy(@NonNull List<Proxy> proxy) {
        proxy.forEach(this::addProxy);
    }

    public void removeAll() {
        proxys.clear();
    }

    @Deprecated
    public void removeProxy(@NonNull Proxy proxy) {
        proxys.remove(proxy);
    }

    public Proxy getRandomProxy() {
        Proxy proxy = proxys.get(ThreadLocalRandom.current().nextInt(proxys.size()));

        return proxy;
    }
}
