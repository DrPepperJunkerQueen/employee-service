package com.example.employeeservice.repository;

import com.example.employeeservice.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    boolean existsBySsnHash(String ssnHash);

    Page<Employee> findAll(Pageable pageable);
}
