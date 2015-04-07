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
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric

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
            def userName = users.signUp().shouldPass()
        then: 'User created in database'
            users.assertUserExist(userName)
    }

    def 'test sign up and activation'() {
        when: 'User send registration request and goes to activation link'
            def username = users.signUpAndActivate().shouldPass();
        then: 'User created in database'
            users.assertUserExist(username)
        and: 'User activated'
            users.assertUserActivated(username)
    }

    def 'registration should fail if honeypot captcha are filled'() {
        when: 'Bot send registration request'
            def username = users.signUpWithHoneypot(honeypot).shouldFail()
        then: 'User not ctreated in database'
            users.assertUserNotExist(username)
        where:
            honeypot    |casename
            "any text"  |"All fields filled"
    }

    def 'registration should fail if all fields are empty'() {
        when: 'User send registration request'
            def name = users.signUp(username, email, password, confirmation)
                    .shouldFailWithAttributeFieldErrors("newUser", "userDto.username", "userDto.email",
                    "userDto.password");
        then: 'User not created in database'
            users.assertUserNotExist(name)
        where:
            username   |email  |password   |confirmation    |caseName
            ""         |""     |""         |""              |"All field are empty"
    }

    def 'registration with invalid username should fail'() {
        when: 'User send registration request with invalid username'
            def name = users.signUpWithUsername(username).shouldFail()
        then: 'User not created in database'
            users.assertUserNotExist(name)
        where:
            username               |caseName
            "   "                  |"Username as spaces"
            randomAlphabetic(26)   |"Username too long"
            ""                     |"Username is empty"
    }

    def 'registration user with valid username should pass'() {
        when: 'User send registration request with valid username'
            def name = users.signUpWithUsername(username).shouldPass()
        then: 'User created in database'
            users.assertUserExist(name)
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
            def username = users.signUpWithEmail(email).shouldFailWithAttributeFieldErrors("newUser", "userDto.email");
        then: 'User not created in database'
            users.assertUserNotExist(username)
        where:
            email                                   |caseName
            randomAlphanumeric(8) + "@" + "jtalks"  |"Invalid email format"
            ""                                      |"Email is empty"
    }

    def 'registration with valid password and confirmation should pass'() {
        when: 'User send registration request with valid password and confirmation'
            def username = users.signUpWithPasswordAndConfirmation(password, password).shouldPass()
        then: 'User created in database'
            users.assertUserExist(username)
        where:
            password                |caseName
            randomAlphanumeric(49)  |"Valid password"
            " "                     |"Space as password"
    }

    def 'registration with invalid password should fail'() {
        when: 'User send registration request with invalid password'
            def username = users.signUpWithPasswordAndConfirmation(password, password)
                    .shouldFailWithAttributeFieldErrors("newUser", "userDto.password")
        then: 'User not created in database'
            users.assertUserNotExist(username)
        where:
            password                |caseName
            ""                      |"Password is empty"
            randomAlphanumeric(51)  |"Too long password"

    }

    def 'registration with different password and confirmation should fail'() {
        when: 'User send registration request with different password and confirmation'
            def username = users.signUpWithPasswordAndConfirmation(password, confirmation)
                    .shouldFailWithAttributeFieldErrors("newUser", "passwordConfirm");
        then: 'User not created in database'
            users.assertUserNotExist(username)
        where:
            password               |confirmation    | caseName
            randomAlphanumeric(10) |""              | "Confirmation is empty"
            "password"             |"PASSWORD"      | "Confirmation in wrong letter case"
            "password"             |" password"     | "Space at the begin of confirmation"
            "password"             |"password "     | "Space at the end of confirmation"
    }

    def 'registration with not unique usename should fail'() {
        given: 'User with default username registered'
            users.signUp();
        when: 'Other user tries to signUp with same username and different email'
            def assertor = users.signUpWithEmail("mail@mail.ru")
        then: 'Username field marked with error'
            assertor.shouldFailWithAttributeFieldErrors("newUser", "userDto.username")
    }

    def 'registration with not unique email should fail'() {
        given: 'User with default email registered'
            users.signUp();
        when: 'Other user tries to signUp with same email and different username'
            def assertor = users.signUpWithUsername("super user")
        then: 'Email field marked with error'
            assertor.shouldFailWithAttributeFieldErrors("newUser", "userDto.email")
    }

}
