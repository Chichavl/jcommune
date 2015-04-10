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
package org.jtalks.jcommune.test

import org.apache.commons.lang.RandomStringUtils
import org.jtalks.jcommune.model.utils.Groups
import org.jtalks.jcommune.test.utils.Users
import org.jtalks.jcommune.test.utils.exceptions.ValidationException
import org.jtalks.jcommune.test.utils.exceptions.WrongResponseException
import org.jtalks.jcommune.test.utils.model.User
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

import static junit.framework.Assert.assertEquals
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
abstract class SignUpTest extends Specification {

    @Autowired
    private WebApplicationContext ctx;

    protected Users users;
    @Autowired
    private Groups groups;

    @Resource(name = "testFilters")
    List<Filter> filters;

    def honeypotErrorResponse;

    abstract void initNonDefaultFailParametersParameters();
    abstract void assertNonDefaultFailParameters(Object expected, WrongResponseException exception);

    def setup() {
        initNonDefaultFailParametersParameters()
        users.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(filters.toArray(new Filter[filters.size()])).build()
        groups.create();
    }

    def 'test sign up success'() {
        when: 'User send registration request'
            def userName = users.singUp(new User())
        then: 'User created in database'
            users.assertUserExist(userName)
    }

    def 'test sign up and activation'() {
        when: 'User send registration request and goes to activation link'
            def username = users.signUpAndActivate(new User())
        then: 'User created in database'
            users.assertUserExist(username)
        and: 'User activated'
            users.assertUserActivated(username)
    }

    def 'registration should fail if honeypot captcha are filled'() {
        when: 'Bot send registration request'
            users.singUp(new User(honeypot: honeypot))
        then: "Wrong response came"
            def e = thrown(WrongResponseException)
            assertNonDefaultFailParameters(honeypotErrorResponse, e)
        and: 'User not created in database'
            users.assertUserNotExist(e.entityIdentifier as String)
        where:
            honeypot    |casename
            "any text"  |"All fields filled"
    }

    def 'registration should fail if all fields are empty'() {
        when: 'User send registration request'
            users.singUp(new User(username: username, email: email, password: password, confirmation: confirmation))
        then: 'Validation error occurs'
            def e = thrown(ValidationException)
            e.getDefaultErrorMessages().containsAll(["Длина имени пользователя должна быть между 1 и 25 символами",
                                                     "Не может быть пустым",
                                                     "Длина пароля должна быть между 1 и 50 символами"])
        and: 'User not created in database'
            users.assertUserNotExist(e.entityIdentifier as String)
        where:
            username   |email  |password   |confirmation    |caseName
            ""         |""     |""         |""              |"All field are empty"
    }

    def 'registration with invalid username should fail'() {
        when: 'User send registration request with invalid username'
            users.singUp(new User(username: username))
        then: 'validation error occurs'
            def e = thrown(ValidationException)
            assertEquals([errorMessage], e.defaultErrorMessages)
        and: 'User not created in database'
            users.assertUserNotExist(e.entityIdentifier as String)
        where:
            username               |errorMessage                                                    |caseName
            "   "                  |"Длина имени пользователя должна быть между 1 и 25 символами"   |"Username as spaces"
            randomAlphabetic(26)   |"Длина имени пользователя должна быть между 1 и 25 символами"   |"Username too long"
            ""                     |"Длина имени пользователя должна быть между 1 и 25 символами"   |"Username is empty"
    }

    def 'registration user with valid username should pass'() {
        when: 'User send registration request with valid username'
            def name = users.singUp(new User(username: username))
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
            users.singUp(new User(email: email))
        then: 'Validation exception occurs'
            def e = thrown(ValidationException)
            assertEquals([errorMessage], e.defaultErrorMessages)
        and: 'User not created in database'
            users.assertUserNotExist(e.entityIdentifier as String)
        where:
            email                                   |errorMessage                           |caseName
            randomAlphanumeric(8) + "@" + "jtalks"  |"Допустимый формат email: mail@mail.ru"|"Invalid email format"
            ""                                      |"Не может быть пустым"                 |"Email is empty"
    }

    def 'registration with valid password and confirmation should pass'() {
        when: 'User send registration request with valid password and confirmation'
            def username = users.singUp(new User(password: password, confirmation: password))
        then: 'User created in database'
            users.assertUserExist(username)
        where:
            password                |caseName
            randomAlphanumeric(49)  |"Valid password"
            " "                     |"Space as password"
    }

    def 'registration with invalid password should fail'() {
        when: 'User send registration request with invalid password'
            users.singUp(new User(password: password, confirmation: password))
        then: 'Validation exception occurs'
            def e = thrown(ValidationException)
        and: 'User not created in database'
            users.assertUserNotExist(e.entityIdentifier as String)
        where:
            password                |errorMessage                                       |caseName
            ""                      |"Длина пароля должна быть между 1 и 50 символами"  |"Password is empty"
            randomAlphanumeric(51)  |"Длина пароля должна быть между 1 и 50 символами"  |"Too long password"

    }

    def 'registration with different password and confirmation should fail'() {
        when: 'User send registration request with different password and confirmation'
            users.singUp(new User(password: password, confirmation: confirmation))
        then: 'Validation exception occurs'
            def e = thrown(ValidationException)
            assertEquals([errorMessage], e.defaultErrorMessages)
        and: 'User not created in database'
            users.assertUserNotExist(e.entityIdentifier as String)
        where:
            password               |confirmation    |errorMessage                                   | caseName
            randomAlphanumeric(10) |""              |"Пароль и подтверждение пароля не совпадают"   | "Confirmation is empty"
            "password"             |"PASSWORD"      |"Пароль и подтверждение пароля не совпадают"   | "Confirmation in wrong letter case"
            "password"             |" password"     |"Пароль и подтверждение пароля не совпадают"   | "Space at the begin of confirmation"
            "password"             |"password "     |"Пароль и подтверждение пароля не совпадают"   | "Space at the end of confirmation"
    }

    def 'registration with not unique usename should fail'() {
        given: 'User registered'
            def username = "amazzzing";
            users.singUp(new User(username: username))
        when: 'Other user tries to signUp with same username'
            users.singUp(new User(username: username))
        then: 'Username field marked with error'
            def e = thrown(ValidationException)
            assertEquals(["Пользователь с таким именем уже существует"], e.defaultErrorMessages)
    }

    def 'registration with not unique email should fail'() {
        given: 'User registered'
            def email = "mail@example.com"
            users.singUp(new User(email: email))
        when: 'Other user tries to signUp with same email and different username'
            users.singUp(new User(email: email))
        then: 'Email field marked with error'
            def e = thrown(ValidationException)
            assertEquals(["Пользователь с таким email уже существует"], e.defaultErrorMessages)
    }

    void setUsers(Users users) {
        this.users = users
    }
}
