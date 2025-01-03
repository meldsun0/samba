package samba.pipeline;

import java.net.InetSocketAddress;

public class Field<T> {

    public static final Field<Object> INCOMING = new Field<>("INCOMING");
    public static final Field<InetSocketAddress> REMOTE_SENDER =
            new Field<>("REMOTE_SENDER"); // InetSocketAddress of remote sender

    private final String name;

    public Field(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Field[" + name + ']';
    }
}
