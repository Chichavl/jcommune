package org.jtalks.jcommune.test.utils.exceptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mikhail Stryzhonok
 */
public class ValidationException extends Exception {
    private Serializable entityIdentifier;
    List<String> defaultErrorMessages = new ArrayList<>();

    public ValidationException(Serializable entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }

    public void addDefaultErrorMessage(String defaultErrorMessage) {
        defaultErrorMessages.add(defaultErrorMessage);
    }

    public Serializable getEntityIdentifier() {
        return entityIdentifier;
    }

    public List<String> getDefaultErrorMessages() {
        return defaultErrorMessages;
    }
}
