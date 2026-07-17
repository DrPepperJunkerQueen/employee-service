package com.example.employeeservice.controller;

import com.example.employeeservice.dto.EmployeeResponse;
import com.example.employeeservice.exception.EmployeeNotFoundException;
import com.example.employeeservice.model.Gender;
import com.example.employeeservice.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void createReturns201AndBodyWithoutSsn() throws Exception {
        UUID id = UUID.randomUUID();
        EmployeeResponse response = new EmployeeResponse(id, "Ada", "Lovelace",
                LocalDate.of(1990, 1, 1), Gender.FEMALE);
        when(employeeService.create(any())).thenReturn(response);

        String requestBody = """
                {
                  "firstName": "Ada",
                  "lastName": "Lovelace",
                  "dateOfBirth": "1990-01-01",
                  "gender": "FEMALE",
                  "socialSecurityNumber": "123-45-6789"
                }
                """;

        mockMvc.perform(post("/employees")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.socialSecurityNumber").doesNotExist())
                .andExpect(jsonPath("$.ssnHash").doesNotExist())
                .andExpect(jsonPath("$.ssn").doesNotExist());
    }

    @Test
    void createReturns400WhenRequiredFieldsMissing() throws Exception {
        String requestBody = """
                {
                  "firstName": "",
                  "lastName": "Lovelace",
                  "dateOfBirth": "1990-01-01",
                  "gender": "FEMALE",
                  "socialSecurityNumber": "not-a-valid-ssn"
                }
                """;

        mockMvc.perform(post("/employees")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void createReturns400WhenBodyMissing() throws Exception {
        mockMvc.perform(post("/employees").contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdReturns200WhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        EmployeeResponse response = new EmployeeResponse(id, "Grace", "Hopper",
                LocalDate.of(1985, 5, 5), Gender.FEMALE);
        when(employeeService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Hopper"));
    }

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(employeeService.getById(id)).thenThrow(new EmployeeNotFoundException(id));

        mockMvc.perform(get("/employees/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getByIdReturns400WhenIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(get("/employees/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsPagedEmployees() throws Exception {
        EmployeeResponse response = new EmployeeResponse(UUID.randomUUID(), "Ada", "Lovelace",
                LocalDate.of(1990, 1, 1), Gender.FEMALE);
        Page<EmployeeResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(employeeService.list(any())).thenReturn(page);

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Ada"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
