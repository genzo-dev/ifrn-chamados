package com.ifrn.chamados.category.dto;

import com.ifrn.chamados.category.Category;

public record CategoryResponseDTO(Long id, String name, String description, Long department_id) {

  public  static  CategoryResponseDTO fromEntity(Category category) {
    return new CategoryResponseDTO(
      category.getId(), 
      category.getName(), 
      category.getDescription(), 
      category.getDepartment().getId()
    );
  }
}