package org.jtalks.jcommune.test.utils;

import org.codehaus.jackson.map.ObjectMapper;
import org.jtalks.jcommune.plugin.api.web.dto.json.JsonResponse;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author Mikhail Stryzhonok
 */
public class JsonResponseUtils {
    private static  ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String jsonResponseToString(JsonResponse response) {
        StringWriter writer = new StringWriter();
        try {
            OBJECT_MAPPER.writeValue(writer, response);
        } finally {
            return writer.toString();
        }
    }
}
