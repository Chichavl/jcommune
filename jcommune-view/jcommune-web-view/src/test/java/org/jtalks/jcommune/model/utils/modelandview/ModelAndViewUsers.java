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
package org.jtalks.jcommune.model.utils.modelandview;

import org.jtalks.jcommune.model.entity.JCUser;
import org.jtalks.jcommune.model.utils.Users;
import org.jtalks.jcommune.model.utils.assertion.ModelAndViewResultAssertor;
import org.jtalks.jcommune.model.utils.assertion.ResultAssertor;
import org.jtalks.jcommune.web.controller.UserController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.http.HttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mikhail Stryzhonok
 */
public class ModelAndViewUsers extends Users {

    @Override
    public HttpSession performLogin() throws Exception{
        return getMockMvc().perform(post("/login")
                .param("userName", USERNAME)
                .param("password", PASSWORD)
                .param("referer", "/"))
                .andReturn().getRequest().getSession();
    }

    @Override
    public ResultAssertor<String> signUp() throws  Exception {
        return buildDefaultResultAssertor(signUp(USERNAME, EMAIL, PASSWORD, CONFIRMATION, HONEYPOT), USERNAME);
    }

    @Override
    public ResultAssertor<String> signUpWithUsername(String username) throws Exception {
        return buildDefaultResultAssertor(signUp(username, EMAIL, PASSWORD, CONFIRMATION, HONEYPOT), username);
    }

    @Override
    public ResultAssertor<String> signUpWithEmail(String email) throws Exception {
        return buildDefaultResultAssertor(signUp(USERNAME, email, PASSWORD, CONFIRMATION, HONEYPOT), USERNAME);
    }

    @Override
    public ResultAssertor<String> signUpWithPasswordAndConfirmation(String password, String confirmation) throws Exception {
        return buildDefaultResultAssertor(signUp(USERNAME, EMAIL, password, confirmation, HONEYPOT), USERNAME);
    }

    @Override
    public ResultAssertor<String> signUpWithHoneypot(String honeypot) throws Exception {
        return buildDefaultResultAssertor(signUp(USERNAME, EMAIL, PASSWORD, CONFIRMATION, honeypot), USERNAME)
                .withFailStatusMatcher(status().isMovedTemporarily())
                .withFailView("redirect:/user/new?reg_error=3");
    }


    @Override
    protected ResultAssertor<String> signUp(String username, String email, String password, String confirmation)
            throws Exception {
        return buildDefaultResultAssertor(signUp(username, email, password, confirmation, HONEYPOT), username);
    }

    @Override
    protected ResultActions signUp(String username, String email, String password, String confirmation,
                                   String honeypot) throws Exception {
        return  getMockMvc().perform(post("/user/new")
                .param("userDto.username", username)
                .param("userDto.email", email)
                .param("userDto.password", password)
                .param("passwordConfirm", confirmation)
                .param("honeypotCaptcha", honeypot));
    }

    private ModelAndViewResultAssertor<String> buildDefaultResultAssertor(ResultActions resultActions, String entityIdentifier) {
        return ModelAndViewResultAssertor.fromResultActions(resultActions)
                .withEntityIdentifier(entityIdentifier)
                .withSuccessStatusMatcher(status().isOk())
                .withSuccessVeiw(UserController.AFTER_REGISTRATION)
                .withFailStatusMatcher(status().isOk())
                .withFailView(UserController.REGISTRATION);
    }
}
