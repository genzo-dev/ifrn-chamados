package com.ifrn.chamados.department.dto;

import com.ifrn.chamados.department.Department;

public record DepartmentResponseDTO(Long id, String name, String abbreviation) {
    
    public static DepartmentResponseDTO fromEntity(Department department) {
        return new DepartmentResponseDTO(
            department.getId(), 
            department.getName(), 
            department.getAbbreviation()
        );
    }
}