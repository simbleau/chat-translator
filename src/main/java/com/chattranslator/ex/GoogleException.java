package com.chattranslator.ex;

/**
 * A generic exception which occurred from bad interaction to Google Cloud Platform.
 *
 * @version January 2021
 * @author <a href="https://spencer.imbleau.com">Spencer Imbleau</a>
 */
public class GoogleException extends Exception {
    public GoogleException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
    public GoogleException(String errorMessage) {
        super(errorMessage);
    }
}
