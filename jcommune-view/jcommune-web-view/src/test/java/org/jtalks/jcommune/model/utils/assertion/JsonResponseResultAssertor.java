/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jtalks.jcommune.model.utils.assertion;

import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jtalks.jcommune.plugin.api.web.dto.json.JsonResponse;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.Assert;

import java.io.StringWriter;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * @author Mikhail Stryzhonok
 */
public class JsonResponseResultAssertor<T> implements ResultAssertor<T> {

    private T entityIdentifier;
    private ResultActions actions;
    private ResultMatcher successStatusMatcher;
    private ResultMatcher failStatusMatcher;
    private ObjectMapper objectMapper = new ObjectMapper();
    private String successResponseBody;
    private String failResponseBody;

    private JsonResponseResultAssertor(ResultActions actions) {
        this.actions = actions;
    }

    public JsonResponseResultAssertor withSuccessStatusMatcher(ResultMatcher statusMathcer) {
        this.successStatusMatcher = statusMathcer;
        return this;
    }

    public JsonResponseResultAssertor withFailStatusMatcher(ResultMatcher statusMatcher) {
        this.failStatusMatcher = statusMatcher;
        return this;
    }

    public JsonResponseResultAssertor withEntityIdentifier(T identifier) {
        this.entityIdentifier = identifier;
        return this;
    }

    public JsonResponseResultAssertor withSuccessJsonResponse(JsonResponse response) {
        successResponseBody = jsonResponseToString(response);
        return this;
    }

    public JsonResponseResultAssertor withFailJsonResponse(JsonResponse response) {
        failResponseBody = jsonResponseToString(response);
        return this;
    }

    private String jsonResponseToString(JsonResponse response) {
        StringWriter writer = new StringWriter();
        try {
            objectMapper.writeValue(writer, response);
        } finally {
            return writer.toString();
        }
    }

    public static JsonResponseResultAssertor fromResultActions(ResultActions actions) {
        return new JsonResponseResultAssertor(actions);
    }


    @Override
    public T shouldPass() throws Exception {
        Assert.notNull(successStatusMatcher, "To assert successful result successStatusMatcher should be initialized");
        Assert.notNull(successResponseBody, "To assert successful result success response should be initialized");
        actions.andExpect(successStatusMatcher).andExpect(content().string(successResponseBody));
        return entityIdentifier;
    }

    @Override
    public T shouldFail() throws Exception {
        Assert.notNull(failStatusMatcher, "To assert fail result failStatusMatcher should be initialized");
        Assert.notNull(failResponseBody, "To assert successful result fail response should be initialized");
        actions.andExpect(failStatusMatcher).andExpect(content().string(failResponseBody));
        return entityIdentifier;
    }

    @Override
    public T shouldFailWithAttributeFieldErrors(String name, String... fieldNames) throws Exception {
        actions.andExpect(failStatusMatcher).andExpect(content().string(new JsonResponseErrorResultMatcher(name,
                fieldNames)));
        return entityIdentifier;
    }

    private static class JsonResponseErrorResultMatcher extends BaseMatcher<String> {

        private String objectName;
        private String[] filedNames;

        public JsonResponseErrorResultMatcher(String objectName, String... fieldNames) {
            this.objectName = objectName;
            this.filedNames = fieldNames;
        }

        @Override
        public boolean matches(Object o) {
            if (o != null && o instanceof String) {
                String responseBody = (String)o;
                StringBuffer buffer = new StringBuffer();
                for (String fieldName : filedNames) {
                    buffer.append("\"objectName\":\"").append(objectName).append("\",\"field\":\"").append(fieldName)
                            .append("\"");
                    if (!responseBody.contains(buffer.toString())) {
                        return false;
                    }
                    buffer.setLength(0);
                }
                return responseBody.contains("\"status\":\"FAIL\"");
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {

        }
    }
}
