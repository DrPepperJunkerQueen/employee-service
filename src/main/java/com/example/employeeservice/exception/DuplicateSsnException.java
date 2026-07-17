package com.example.employeeservice.exception;

public class DuplicateSsnException extends RuntimeException {

    public DuplicateSsnException() {
        super("An employee with this social security number already exists");
    }
}
