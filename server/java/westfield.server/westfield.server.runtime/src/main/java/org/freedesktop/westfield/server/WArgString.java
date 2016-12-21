package org.freedesktop.westfield.server;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class WArgString implements WArg {

    private final String arg;

    WArgString(final String arg) {
        this.arg = arg;
    }

    public void write(ByteBuffer byteBuffer) {
        byteBuffer.putInt(arg.length());
        byteBuffer.put(arg.getBytes(StandardCharsets.US_ASCII));
        //align to the next integer
        byteBuffer.position((byteBuffer.position() + 3) & ~3);
    }

    public int size() {
        //returns integer aligned size
        return Integer.BYTES + ((arg.length() + 3) & ~3);
    }
}