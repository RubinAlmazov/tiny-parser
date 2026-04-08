package org.me.mytinyparser;


import jakarta.servlet.http.HttpServletRequest;
import org.me.mytinyparser.utils.ParserUtils;

public class MyTinyParserApplication {



    public static void main(String[] args) {
        ParserUtils parserUtils = new ParserUtils();
        byte[] f = "1234".getBytes();
        byte[] s = "boundary".getBytes();
        System.out.println(parserUtils.containsSubArray(f,s));
    }



}
