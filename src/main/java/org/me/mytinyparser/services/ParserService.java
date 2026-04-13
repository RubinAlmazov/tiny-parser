package org.me.mytinyparser.services;

import jakarta.servlet.http.HttpServletRequest;
import org.me.mytinyparser.enums.States;
import org.me.mytinyparser.utils.BoundedInputStream;
import org.me.mytinyparser.utils.ContentDisposition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.me.mytinyparser.enums.States.*;
import static org.me.mytinyparser.utils.ParserUtils.containsSubArray;

public class ParserService {

    private final HttpServletRequest httpServletRequest;
    private final byte[] boundaryName;
    private States state = LOOKING_BOUNDARY;
    private final String rawBoundary;

    public ParserService(HttpServletRequest servletRequest) {
        this.httpServletRequest = servletRequest;

        String contentType = httpServletRequest.getContentType();
        if (contentType != null && contentType.contains("boundary=")) {
            rawBoundary = contentType.substring(contentType.indexOf("boundary=") +9);
            this.boundaryName = ("--" + rawBoundary).getBytes();
        }
        else {
            throw new RuntimeException();
        }
    }


    public ContentDisposition extractContentDisposition(InputStream inputStream) {
        byte[] buffer = new byte[8192];
        int byteRead;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StringBuilder dispositionContent = new StringBuilder();


        try {
            while (state != FOUND_DISPOSITION && (byteRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteRead);
                int boundaryIndex = containsSubArray(outputStream.toByteArray(), boundaryName);

                if (state != FOUND_BOUNDARY &&  boundaryIndex != -1) {
                    state = FOUND_BOUNDARY;
                }

                if (state == FOUND_BOUNDARY) {
                    String streamContent = outputStream.toString(StandardCharsets.UTF_8);
                    if (streamContent.contains("\r\n\r\n")) {
                        state = FOUND_DISPOSITION;
                    }

                    if (boundaryIndex != -1) {
                        dispositionContent.append(streamContent.substring(boundaryIndex));
                    }
                    else {
                        dispositionContent.append(streamContent);
                    }

                    if (state != FOUND_DISPOSITION) {
                        int tailStart = Math.max(0, streamContent.length() - 3);
                        byte[] tail = streamContent.substring(tailStart).getBytes();
                        outputStream.reset();
                        outputStream.write(tail);
                    }
                    else {
                        outputStream.reset();
                    }
                }

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        state = LOOKING_BOUNDARY;

        return parseContentDisposition(dispositionContent.toString());
    }

    public BoundedInputStream extractResourceContent(InputStream inputStream) throws IOException {
        byte[] boundary = ("\r\n--" + rawBoundary).getBytes();
        return new BoundedInputStream(inputStream, boundary);
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

}
