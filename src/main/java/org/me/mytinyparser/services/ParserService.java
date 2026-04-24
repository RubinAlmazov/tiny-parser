package org.me.mytinyparser.services;

import jakarta.servlet.http.HttpServletRequest;
import org.me.mytinyparser.enums.States;
import org.me.mytinyparser.utils.BoundedInputStream;
import org.me.mytinyparser.utils.ContentDisposition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.me.mytinyparser.enums.States.*;
import static org.me.mytinyparser.utils.ParserUtils.containsSubArray;

public class ParserService {

    private final byte[] boundaryName;
    private States state = LOOKING_BOUNDARY;
    private final String rawBoundary;
    private final InputStream inputStream;
    private byte[] leftover = new byte[0];

    public ParserService(HttpServletRequest servletRequest) throws IOException {
        String contentType = servletRequest.getContentType();
        if (contentType != null && contentType.contains("boundary=")) {
            rawBoundary = contentType.substring(contentType.indexOf("boundary=") + 9);
            this.boundaryName = ("--" + rawBoundary).getBytes();
        } else {
            throw new RuntimeException();
        }
        this.inputStream = servletRequest.getInputStream();
    }


    public ContentDisposition extractContentDisposition() {
        byte[] buffer = new byte[8192];
        int byteRead = 0;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StringBuilder dispositionContent = new StringBuilder();

        try {
            while (state != FOUND_DISPOSITION && (byteRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteRead);
                int boundaryIndex = containsSubArray(outputStream.toByteArray(), boundaryName);

                if (state != FOUND_BOUNDARY && boundaryIndex != -1) {
                    state = FOUND_BOUNDARY;
                }

                if (state == FOUND_BOUNDARY) {
                    String streamContent = outputStream.toString(StandardCharsets.UTF_8);
                    if (streamContent.contains("\r\n\r\n")) {
                        state = FOUND_DISPOSITION;
                    }

                    if (boundaryIndex != -1) {
                        dispositionContent.append(streamContent.substring(boundaryIndex));
                    } else {
                        dispositionContent.append(streamContent);
                    }

                    if (state != FOUND_DISPOSITION) {
                        int tailStart = Math.max(0, outputStream.size() - (boundaryName.length - 1));
                        byte[] tail = outputStream.toByteArray();
                        tail = java.util.Arrays.copyOfRange(tail, tailStart, tail.length);
                        outputStream.reset();
                        outputStream.write(tail);
                    } else {
                        byte[] all = outputStream.toByteArray();
                        int sepEnd = containsSubArray(all, new byte[] {'\r', '\n', '\r', '\n'});
                        if (sepEnd != -1 && sepEnd < all.length) {
                            leftover = Arrays.copyOfRange(all, sepEnd, all.length);
                        }
                        outputStream.reset();
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        state = (byteRead != -1) ? LOOKING_BOUNDARY : REACHED_END;

        String raw = dispositionContent.toString();
        if (state == REACHED_END && !raw.contains("Content-Disposition:")) {
            return null;
        }

        return parseContentDisposition(raw);
    }

    public BoundedInputStream extractResourceContent() throws IOException {
        byte[] boundary = ("\r\n--" + rawBoundary).getBytes();
        InputStream source = leftover.length > 0
                ? new SequenceInputStream(new ByteArrayInputStream(leftover), inputStream)
                : inputStream;
        leftover = new byte[0];
        return new BoundedInputStream(source, boundary);
    }

    private ContentDisposition parseContentDisposition(String raw) {
        int start = raw.indexOf("Content-Disposition:") + "Content-Disposition:".length();
        int end = raw.indexOf("\r\n", start);
        String line = raw.substring(start, end).trim();

        String type = line.contains(";")
                ? line.substring(0, line.indexOf(";")).trim()
                : line.trim();

        return new ContentDisposition(extractParam(line, "filename"), extractParam(line, "name"), type);
    }

    private String extractParam(String line, String param) {
        String key = param + "=\"";
        int start = line.indexOf(key);
        if (start == -1) return null;
        start += key.length();
        int end = line.indexOf("\"", start);
        return line.substring(start, end);
    }

    public States getState() {
        return state;
    }
}
