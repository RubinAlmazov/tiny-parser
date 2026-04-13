package org.me.mytinyparser.services;

import jakarta.servlet.http.HttpServletRequest;
import org.me.mytinyparser.enums.States;
import org.me.mytinyparser.utils.BoundedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.me.mytinyparser.enums.States.*;
import static org.me.mytinyparser.utils.ParserUtils.containsSubArray;

public class ParserService {

    private final HttpServletRequest httpServletRequest;
    private final byte[] boundaryName;
    private States state = FINDING_BOUNDARY;

    public ParserService(HttpServletRequest servletRequest) {
        this.httpServletRequest = servletRequest;

        String contentType = httpServletRequest.getContentType();
        if (contentType != null && contentType.contains("boundary=")) {
            this.boundaryName = contentType.substring(contentType.indexOf("boundary=") +9).getBytes();
        }
        else {
            throw new RuntimeException();
        }
    }


    public String extractContentDisposition(InputStream inputStream) {
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

                    int tailStart = Math.max(0, streamContent.length() - 3);
                    byte[] tail = streamContent.substring(tailStart).getBytes();
                    outputStream.reset();
                    outputStream.write(tail);
                }

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dispositionContent.toString();
    }

    public BoundedInputStream extractResourceContent(InputStream inputStream) throws IOException {
        BoundedInputStream boundedInputStream = new BoundedInputStream(inputStream, boundaryName);

        return boundedInputStream;
    }

}
