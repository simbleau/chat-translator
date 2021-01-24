package com.chattranslator.ex;

/**
 * An exception generated from invalid authentication to Google Cloud Platform.
 *
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 */
public class GoogleAuthenticationException extends GoogleException {
    public GoogleAuthenticationException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
    public GoogleAuthenticationException(String errorMessage) {
        super(errorMessage);
    }
}
