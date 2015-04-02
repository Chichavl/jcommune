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
package org.jtalks.jcommune.web.component

import org.jtalks.jcommune.model.utils.Groups
import org.jtalks.jcommune.model.utils.Users
import org.jtalks.jcommune.web.controller.UserController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import javax.annotation.Resource
import javax.servlet.Filter

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

/**
 * @author Mikhail Stryzhonok
 */
@WebAppConfiguration
@ContextConfiguration(locations = [
        "classpath:/org/jtalks/jcommune/model/entity/applicationContext-dao.xml",
        "classpath:/org/jtalks/jcommune/model/entity/applicationContext-properties.xml",
        "classpath:/org/jtalks/jcommune/service/applicationContext-service.xml",
        "classpath:/org/jtalks/jcommune/service/security-service-context.xml",
        "classpath:/org/jtalks/jcommune/service/email-context.xml",
        "classpath:/org/jtalks/jcommune/web/applicationContext-controller.xml",
        "classpath:security-context.xml",
        "classpath:spring-dispatcher-servlet.xml",
        "classpath:/org/jtalks/jcommune/web/view/test-configuration.xml"
])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
class SignUpTest extends Specification {

    @Autowired
    private WebApplicationContext ctx;
    @Autowired
    private Users users;
    @Autowired
    private Groups groups;

    @Resource(name = "testFilters")
    List<Filter> filters;

    def setup() {
        users.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(filters.toArray(new Filter[filters.size()])).build()
        groups.create();
    }

    def 'test sign up success'() {
        when: 'User send registration request'
            def result = users.registerDefault()
        then: 'After registration page is shown'
            result.andExpect(status().isOk()).andExpect(view().name(UserController.AFTER_REGISTRATION))
        and: 'User created in database'
            users.assertDefaultUserExist()
    }

    def 'test sign up and activation'() {
        when: 'User send registration request and goes to activation link'
            def result = users.registerAndActivate()
        then: 'User redirected to main page'
            result.andExpect(status().isMovedTemporarily()).andExpect(redirectedUrl("/"))
        and: 'User created in database'
            users.assertDefaultUserExist()
        and: 'User activated'
            users.assertDefaultUserActivated()
    }

    def 'registration should fail if honeypot captcha are filled'() {
        when: 'Bot send registration request'
            def result = users.registerWithHoneypot(honeypot)
        then: 'Bot redirected back to registration page with error'
            result.andExpect(status().isMovedTemporarily()).andExpect(redirectedUrl("/user/new?reg_error=3"))
        and: 'User not ctreated in database'
            users.assertDefaultUserNotExist()
        where:
            honeypot    |casename
            "any text"  |"All fields filled"
    }

    def 'registration should fail if all fields are empty'() {
        when: 'User send registration request'
            def result = users.register(username, email, password, confirmation)
        then: 'Registration page is shown'
            result.andExpect(status().isOk()).andExpect(view().name(UserController.REGISTRATION))
        and: 'All fields marked with error'
            result.andExpect(model().attributeHasFieldErrors("newUser", "userDto.username", "userDto.email",
                    "userDto.password"))
        and: 'User not created in database'
            users.assertUserNotExist(username)
        where:
            username   |email  |password   |confirmation    |caseName
            ""         |""     |""         |""              |"All field are empty"
    }

    def 'registration with invalid username should fail'() {
        when: 'User send registration request with invalid username'
            def result = users.registerWithUsername(username)
        then: 'Registration page is shown'
            result.andExpect(status().isOk()).andExpect(view().name(UserController.REGISTRATION))
        and: 'Username field marked with error'
            result.andExpect(model().attributeHasFieldErrors("newUser", "userDto.username"))
        and: 'User not created in database'
            users.assertUserNotExist(username)
        where:
            username               |caseName
            "   "                  |"Username as spaces"
            randomAlphabetic(26)   |"Username too long"
            ""                     |"Username is empty"
    }

    def 'registration user with valid username should pass'() {
        when: 'User send registration request with valid username'
            def result = users.registerWithUsername(username)
        then: 'After registration page is shown'
            result.andExpect(status().isOk()).andExpect(view().name(UserController.AFTER_REGISTRATION))
        and: 'User created in database'
            users.assertUserExist(username)
        where:
            username                                                    |caseName
            randomAlphanumeric(14)                                      |"Length of username between 1 and 25"
            randomAlphanumeric(8) + " " + randomAlphanumeric(8)         |"Username contains spaces"
            randomAlphanumeric(25)                                      |"Username has max allowed length"
            "/" + randomAlphanumeric(8)                                 |"Username contains slash"
            "\\" + randomAlphanumeric(8)                                |"Username contains back slash"
            randomAlphanumeric(5) + "      " + randomAlphanumeric(5)    |"Username contains several spaces in the middle"
    }

    def 'registration with invalid email should fail'() {
        when: 'User send registration request with invalid email'
            def result = users.registerWithEmail(email)
        then: 'Registration page is shown'
            result.andExpect(status().isOk()).andExpect(view().name(UserController.REGISTRATION))
        and: 'Email field marked with error'
            result.andExpect(model().attributeHasFieldErrors("newUser", "userDto.email"))
        and: 'User not created in database'
            users.assertDefaultUserNotExist()
        where:
            email                                   |caseName
            randomAlphanumeric(8) + "@" + "jtalks"  |"Invalid email format"
            ""                                      |"Email is empty"
    }

    def 'registration with valid password and confirmation should pass'() {
        when: 'User send registration request with valid password and confirmation'
            def result = users.registerWithPasswordAndConfirmation(password, password)
        then: 'After registration page is shown'
            result.andExpect(status().isOk()).andExpect(view().name(UserController.AFTER_REGISTRATION))
        and: 'User created in database'
            users.assertDefaultUserExist()
        where:
            password                |caseName
            randomAlphanumeric(49)  |"Valid password"
            " "                     |"Space as password"
    }

    def 'registration with invalid password should fail'() {
        when: 'User send registration request with invalid password'
            def result = users.registerWithPasswordAndConfirmation(password, password)
        then: 'Registration page is shown'
            result.andExpect(status().isOk()).andExpect(view().name(UserController.REGISTRATION))
        and: 'Password field marked with error'
            result.andExpect(model().attributeHasFieldErrors("newUser", "userDto.password"))
        and: 'User not created in database'
            users.assertDefaultUserNotExist()
        where:
            password                |caseName
            ""                      |"Password is empty"
            randomAlphanumeric(51)  |"Too long password"

    }

    def 'registration with different password and confirmation should fail'() {
        when: 'User send registration request with different password and confirmation'
            def result = users.registerWithPasswordAndConfirmation(password, confirmation)
        then: 'Registration page is shown'
            result.andExpect(status().isOk()).andExpect(view().name(UserController.REGISTRATION))
        and: 'Password confirmation field marked with error'
            result.andExpect(model().attributeHasFieldErrors("newUser", "passwordConfirm"))
        and: 'User not created in database'
            users.assertDefaultUserNotExist()
        where:
            password               |confirmation    | caseName
            randomAlphanumeric(10) |""              | "Confirmation is empty"
            "password"             |"PASSWORD"      | "Confirmation in wrong letter case"
            "password"             |" password"     | "Space at the begin of confirmation"
            "password"             |"password "     | "Space at the end of confirmation"
    }

    def 'registration with not unique usename should fail'() {
        given: 'User with default username registered'
            users.registerDefault();
        when: 'Other user tries to register with same username and different email'
            def result = users.registerWithEmail("mail@mail.ru")
        then: 'Registration page is shown'
            result.andExpect(status().isOk()).andExpect(view().name(UserController.REGISTRATION))
        and: 'Username field marked with error'
            result.andExpect(model().attributeHasFieldErrors("newUser", "userDto.username"))
    }

    def 'registration with not unique email should fail'() {
        given: 'User with default email registered'
            users.registerDefault();
        when: 'Other user tries to register with same email and different username'
            def result = users.registerWithUsername("super user")
        then: 'Registration page is shown'
            result.andExpect(status().isOk()).andExpect(view().name(UserController.REGISTRATION))
        and: 'Email field marked with error'
            result.andExpect(model().attributeHasFieldErrors("newUser", "userDto.email"))
    }

}
