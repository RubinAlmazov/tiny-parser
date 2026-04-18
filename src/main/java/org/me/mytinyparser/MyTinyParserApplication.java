package org.me.mytinyparser;


import jakarta.servlet.http.HttpServletRequest;
import org.me.mytinyparser.dto.Parts;
import org.me.mytinyparser.utils.BoundedInputStream;
import static org.me.mytinyparser.enums.States.*;
import org.me.mytinyparser.services.ParserService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyTinyParserApplication {

    public List<Parts> parseAll(HttpServletRequest request) throws IOException {
        List<Parts> partsList = new ArrayList<>();

        ParserService parserService = new ParserService(request);

        while (parserService.getState() != REACHED_END) {
            var contentDisposition = parserService.extractContentDisposition();

            if (parserService.getState() == REACHED_END) break;

            BoundedInputStream content = parserService.extractResourceContent();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            content.transferTo(buffer);

            partsList.add(new Parts(contentDisposition, new ByteArrayInputStream(buffer.toByteArray()), buffer.size()));
        }

        return partsList;
    }





}
