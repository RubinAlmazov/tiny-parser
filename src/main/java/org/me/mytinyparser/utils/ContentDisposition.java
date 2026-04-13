package org.me.mytinyparser.utils;

public class ContentDisposition {
    private final String fileName;
    private final String name;
    private final String type;

    public ContentDisposition(String fileName, String name, String type) {
        this.fileName = fileName;
        this.name = name;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
