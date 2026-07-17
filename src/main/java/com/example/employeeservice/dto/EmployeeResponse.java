package com.example.employeeservice.dto;

import com.example.employeeservice.model.Employee;
import com.example.employeeservice.model.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Outbound representation of an employee.
 *
 * Deliberately has no SSN / ssnHash field whatsoever - not masked, not
 * partially shown, simply absent - so it is structurally impossible for
 * any endpoint using this DTO to leak the social security number,
 * hashed or otherwise.
 */
public class EmployeeResponse {

    private final UUID id;
    private final String firstName;
    private final String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;

    private final Gender gender;

    public EmployeeResponse(UUID id, String firstName, String lastName, LocalDate dateOfBirth, Gender gender) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDateOfBirth(),
                employee.getGender()
        );
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
}
