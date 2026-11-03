package com.vriddhi.exam.exception;

/** Thrown when a write is attempted against an attempt whose server-side deadline has passed. */
public class AttemptExpiredException extends RuntimeException {
    public AttemptExpiredException(String message) { super(message); }
}
