package com.example.employeeservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Persistence entity for an employee record.
 *
 * Important: this entity intentionally has NO field for the raw social
 * security number. Only {@link #ssnHash}, a one-way HMAC-SHA256 digest,
 * is ever stored. The plaintext SSN exists only transiently in memory
 * while handling a create request and is never written to the database,
 * logged, or returned in any response - see SsnHashService for details
 * and README.md for the reasoning behind hashing vs. encryption here.
 */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    /**
     * HMAC-SHA256(ssn, secret) hex-encoded. Unique so we can reject
     * duplicate employee records without ever storing or comparing
     * plaintext SSNs.
     */
    @Column(name = "ssn_hash", nullable = false, unique = true, length = 64)
    private String ssnHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Employee() {
        // JPA
    }

    public Employee(String firstName, String lastName, LocalDate dateOfBirth, Gender gender, String ssnHash) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.ssnHash = ssnHash;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public String getSsnHash() {
        return ssnHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
