package ru.serykhd.http.response;


import lombok.Getter;
import ru.serykhd.common.time.util.TimeUtils;

@Getter
@Deprecated(since = "Переделать, а то хуйня какая-то")
public class ResponseStatistics {

    // https://developer.chrome.com/docs/devtools/network/reference/?utm_source=devtools#timing-explanation

    private long initialConnection;
    private long waitingFirstByte;
    private long contentDownloading;

    private long last = System.nanoTime();

    public void setInitialConnection() {
        long curr = System.nanoTime();

        initialConnection = curr - last;

        last = curr;
    }

    public void setWaitingFirstByte() {
        long curr = System.nanoTime();

        waitingFirstByte = curr - last;

        last = curr;
    }

    public void setContentDownloading() {
        long curr = System.nanoTime();

        contentDownloading = curr - last;

        last = curr;

     //   System.out.println(toString());
    }

    @Override
    public String toString() {
        return "ResponseStatistics{" +
                "initialConnection=" + getElapsed(initialConnection) +
                ", waitingFirstByte=" + getElapsed(waitingFirstByte) +
                ", contentDownloading=" + getElapsed(contentDownloading) +
              //  ", last=" + last +
                '}';
    }

    private static String getElapsed(long v) {
        return String.format("%sms", TimeUtils.toMillis(v));
    }
}
