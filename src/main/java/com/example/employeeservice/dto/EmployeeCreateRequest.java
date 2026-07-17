package com.example.employeeservice.dto;

import com.example.employeeservice.model.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Inbound payload for POST /employees.
 *
 * socialSecurityNumber is only ever held here transiently - it is hashed
 * in the service layer before anything is persisted, and this class is
 * never reused as a response type, so there is no risk of it being
 * echoed back in a response body.
 */
public class EmployeeCreateRequest {

    @NotBlank(message = "firstName is required")
    private String firstName;

    @NotBlank(message = "lastName is required")
    private String lastName;

    @NotNull(message = "dateOfBirth is required")
    @Past(message = "dateOfBirth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotNull(message = "gender is required")
    private Gender gender;

    @NotBlank(message = "socialSecurityNumber is required")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{4}$", message = "socialSecurityNumber must match format XXX-XX-XXXX")
    private String socialSecurityNumber;

    public EmployeeCreateRequest() {
        // Jackson
    }

    public EmployeeCreateRequest(String firstName, String lastName, LocalDate dateOfBirth,
                                  Gender gender, String socialSecurityNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.socialSecurityNumber = socialSecurityNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }
}
