package org.me.mytinyparser.dto;

import org.me.mytinyparser.utils.ContentDisposition;

import java.io.InputStream;

public record Parts(
        ContentDisposition contentDisposition,
        InputStream resourceContent,
        int size
) {
}



