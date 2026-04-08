package org.me.mytinyparser.dto;

import java.io.InputStream;
import java.util.Map;

public record Parts(
        Map<String, String> headers,
        String fileName,
        InputStream resourceContent
) {
}



