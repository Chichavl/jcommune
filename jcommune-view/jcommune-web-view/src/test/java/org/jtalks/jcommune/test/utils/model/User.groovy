package org.jtalks.jcommune.test.utils.model

import org.apache.commons.lang.RandomStringUtils

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic


/**
 * @author Mikhail Stryzhonok
 */
class User {
    String username = randomAlphabetic(25)
    String email = randomAlphabetic(40) + "@sample.ru"
    String password = randomAlphabetic(50)
    String confirmation = password
    String honeypot = ""
}
