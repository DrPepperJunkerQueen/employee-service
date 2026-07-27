package com.example.employeeservice.service;

import com.example.employeeservice.dto.EmployeeCreateRequest;
import com.example.employeeservice.dto.EmployeeResponse;
import com.example.employeeservice.exception.DuplicateSsnException;
import com.example.employeeservice.exception.EmployeeNotFoundException;
import com.example.employeeservice.model.Employee;
import com.example.employeeservice.repository.EmployeeRepository;
import com.example.employeeservice.security.SsnHashService;
import com.example.employeeservice.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SsnHashService ssnHashService;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, SsnHashService ssnHashService) {
        this.employeeRepository = employeeRepository;
        this.ssnHashService = ssnHashService;
    }

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest request) {
        String ssnHash = ssnHashService.hash(request.getSocialSecurityNumber());

        // Belt-and-braces: checked here for a fast, clear 409, and also
        // backed by a DB-level unique constraint on ssn_hash in case of
        // a race between concurrent requests.
        if (employeeRepository.existsBySsnHash(ssnHash)) {
            throw new DuplicateSsnException();
        }

        Employee employee = new Employee(
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth(),
                request.getGender(),
                ssnHash
        );

        Employee saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return EmployeeResponse.from(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> list(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(EmployeeResponse::from);
    }
}
