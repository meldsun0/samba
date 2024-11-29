package samba.util;

import java.net.InetSocketAddress;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public class NetworkUtil {

    public static InetSocketAddress convertToInetSocketAddress(String socket) throws Exception {
        checkNotNull(socket, "Socket cannot be null");
        checkArgument(!socket.contains(":") || socket.split(":").length == 2, "Invalid socket format. Must be 'hostname:port'");
        try {
            String[] parts = socket.split(":");
            String host = parts[0];
            return new InetSocketAddress(host, Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid socket format");
        }
    }
}
