package com.chattranslator.ex;

/**
 * An exception generated from a failed API call to Google Cloud Platform.
 *
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 */
public class GoogleAPIException extends GoogleException {
    public GoogleAPIException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
    public GoogleAPIException(String errorMessage) {
        super(errorMessage);
    }
}
