package org.bih.aft.exceptions;

import org.ehrbase.openehr.sdk.util.exception.SdkException;

public class InvalidCountQuery extends SdkException {
    public InvalidCountQuery(String message) {
        super(message);
    }
}
