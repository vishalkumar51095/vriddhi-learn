package com.vriddhi.exam.exception;

/** A service this one depends on (content-service, progress-service) could not be reached. Maps to HTTP 503. */
public class UpstreamUnavailableException extends RuntimeException {
    public UpstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
