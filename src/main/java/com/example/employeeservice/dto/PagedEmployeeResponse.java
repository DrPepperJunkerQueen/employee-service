package com.example.employeeservice.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class PagedEmployeeResponse {

    private final List<EmployeeResponse> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public PagedEmployeeResponse(List<EmployeeResponse> content, int page, int size,
                                  long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static PagedEmployeeResponse from(Page<EmployeeResponse> page) {
        return new PagedEmployeeResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public List<EmployeeResponse> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
