package com.example.employeeservice.model;

/**
 * Deliberately kept small and explicit rather than a free-text string,
 * so invalid values are rejected at deserialization time (400) instead
 * of silently persisted.
 */
public enum Gender {
    MALE,
    FEMALE,
    NONBINARY,
    OTHER,
    PREFER_NOT_TO_SAY
}
