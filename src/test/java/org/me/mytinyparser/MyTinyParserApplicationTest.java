//package org.me.mytinyparser;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class MyTinyParserApplicationTest {
//
//    @Test
//    void testExtractBoundary_shouldReturnBoundary() {
//        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
//        when(httpServletRequest.getContentType()).thenReturn("boundary=abc123");
//
//        String boundary = MyTinyParserApplication.extractBoundary(httpServletRequest);
//
//        assertEquals("boundary=abc123", boundary);
//    }
//}