package org.me.mytinyparser.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BoundedInputStream extends InputStream {
    private final InputStream source;
    private final byte[] boundary;
    private final byte[] buffer;
    private int tailPos = -1;

    public BoundedInputStream(InputStream stream, byte[] boundary) throws IOException {
        this.source = stream;
        this.boundary = boundary;
        this.buffer = source.readNBytes(boundary.length);
        if (this.buffer.length < boundary.length) {
            this.tailPos = 0;
        }
    }

    @Override
    public int read() throws IOException {
        if (tailPos != -1) {
            return tailPos < buffer.length ? (buffer[tailPos++] & 0xFF): -1;
        }

        if (Arrays.equals(buffer, boundary)) {return -1;}

        int head = buffer[0] & 0xFF;

        int byteRead = source.read();
        if (byteRead == -1) {
            tailPos = 1;
            return head;
        }

        System.arraycopy(buffer,1, buffer, 0, buffer.length-1 );
        buffer[buffer.length-1] = (byte) byteRead;

        return head;
    }

}
