package ru.serykhd.http;

import io.netty.util.NetUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import static java.lang.System.out;

@Deprecated
public class v6 {

    public static void main(String args[]) throws SocketException {
        System.setProperty("java.net.preferIPv6Addresses", "true");

        out.println(NetUtil.isIpV6AddressesPreferred());
        /*
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets))
            displayInterfaceInformation(netint);

         */
    }

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        out.printf("Display name: %s\n", netint.getDisplayName());
        out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            out.printf("InetAddress: %s\n", inetAddress);
        }
        out.printf("\n");
    }
}
