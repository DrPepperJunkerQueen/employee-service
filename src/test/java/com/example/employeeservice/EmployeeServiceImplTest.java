package com.example.employeeservice.service;

import com.example.employeeservice.dto.EmployeeCreateRequest;
import com.example.employeeservice.dto.EmployeeResponse;
import com.example.employeeservice.exception.DuplicateSsnException;
import com.example.employeeservice.exception.EmployeeNotFoundException;
import com.example.employeeservice.model.Employee;
import com.example.employeeservice.model.Gender;
import com.example.employeeservice.repository.EmployeeRepository;
import com.example.employeeservice.security.SsnHashService;
import com.example.employeeservice.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private final SsnHashService ssnHashService = new SsnHashService("test-secret");

    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository, ssnHashService);
    }

    private EmployeeCreateRequest sampleRequest() {
        return new EmployeeCreateRequest(
                "Ada",
                "Lovelace",
                LocalDate.of(1990, 1, 1),
                Gender.FEMALE,
                "123-45-6789"
        );
    }

    @Test
    void createPersistsHashedSsnNotRawSsn() {
        when(employeeRepository.existsBySsnHash(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = employeeService.create(sampleRequest());

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());

        Employee saved = captor.getValue();
        String expectedHash = ssnHashService.hash("123-45-6789");

        assertThat(saved.getSsnHash()).isEqualTo(expectedHash);
        assertThat(saved.getSsnHash()).doesNotContain("123456789");
        assertThat(response.getFirstName()).isEqualTo("Ada");
    }

    @Test
    void createRejectsDuplicateSsn() {
        when(employeeRepository.existsBySsnHash(anyString())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(sampleRequest()))
                .isInstanceOf(DuplicateSsnException.class);

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void responseNeverExposesSsnOrHash() {
        when(employeeRepository.existsBySsnHash(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = employeeService.create(sampleRequest());

        // EmployeeResponse has no SSN-related getter at all - this test
        // documents that guarantee at the type level. If someone adds an
        // ssn/ssnHash field to EmployeeResponse in the future, update this
        // test explicitly rather than letting it slip through silently.
        assertThat(response.getClass().getDeclaredFields())
                .noneMatch(field -> field.getName().toLowerCase().contains("ssn"));
    }

    @Test
    void getByIdReturnsEmployeeWhenFound() {
        UUID id = UUID.randomUUID();
        Employee employee = new Employee("Grace", "Hopper", LocalDate.of(1985, 5, 5),
                Gender.FEMALE, ssnHashService.hash("111-22-3333"));
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        EmployeeResponse response = employeeService.getById(id);

        assertThat(response.getFirstName()).isEqualTo("Grace");
        assertThat(response.getLastName()).isEqualTo("Hopper");
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getById(id))
                .isInstanceOf(EmployeeNotFoundException.class);
    }
}
