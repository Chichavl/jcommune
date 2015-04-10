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
package org.jtalks.jcommune.test.utils.popup;

import com.google.common.collect.ImmutableMap;
import org.jtalks.jcommune.test.utils.JsonResponseUtils;
import org.jtalks.jcommune.test.utils.Users;
import org.jtalks.jcommune.plugin.api.web.dto.json.JsonResponse;
import org.jtalks.jcommune.plugin.api.web.dto.json.JsonResponseStatus;
import org.jtalks.jcommune.test.utils.exceptions.WrongResponseException;
import org.jtalks.jcommune.test.utils.model.User;
import org.jtalks.jcommune.web.controller.UserController;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.http.HttpSession;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mikhail Stryzhonok
 */
public class PopUpUsers extends Users {

    @Override
    public HttpSession performLogin() throws Exception {
        return getMockMvc().perform(post("/login_ajax")
                .param("userName", USERNAME).param("password", PASSWORD)).andDo(print())
                .andReturn().getRequest().getSession();
    }

    @Override
    public void assertMvcResult(MvcResult result, Serializable entityIdentifier) throws WrongResponseException {
        try {
            if(!result.getResponse().getContentAsString()
                    .equals(JsonResponseUtils.jsonResponseToString(new JsonResponse(JsonResponseStatus.SUCCESS)))) {
                //TODO: json parsing
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String singUp(User user) throws Exception {
        assertMvcResult(getMockMvc().perform(post("/user/new_ajax")
                .param("userDto.username", user.getUsername())
                .param("userDto.email", user.getEmail())
                .param("userDto.password", user.getPassword())
                .param("passwordConfirm", user.getConfirmation())
                .param("honeypotCaptcha", user.getHoneypot())).andReturn(), user.getUsername());
        return user.getUsername();
    }

}
