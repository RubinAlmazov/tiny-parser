package org.me.mytinyparser.utils;

public class ParserUtils {

    public static int containsSubArray(byte[] source, byte[] toFind) {
        int index = 0;
        int i;
        for (i = 0; i < source.length; i++) {
            if (index < toFind.length) {
                if (source[i] == toFind[index]) {
                    index++;
                }
                else {
                    index = 0;
                    if (source[i] == toFind[index]) {
                        index = 1;
                    }
                }
            } else {
                break;
            }
        }

        return index == toFind.length ? i : -1;
    }
}
