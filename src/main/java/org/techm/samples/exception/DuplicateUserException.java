package org.techm.samples.exception;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String email) {
        super("A user with email already exists.");
    }
}
