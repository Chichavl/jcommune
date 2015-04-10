package org.jtalks.jcommune.test.utils.exceptions;

import java.io.Serializable;

/**
 * @author Mikhail Stryzhonok
 */
public class WrongResponseException extends Exception {

    private Serializable entityIdentifier;
    private Object expected;
    private Object actual;

    public WrongResponseException(Serializable entityIdentifier, Object expected, Object actual) {
        this.entityIdentifier = entityIdentifier;
        this.expected = expected;
        this.actual = actual;
    }

    public Serializable getEntityIdentifier() {
        return entityIdentifier;
    }

    public Object getExpected() {
        return expected;
    }

    public Object getActual() {
        return actual;
    }

}
