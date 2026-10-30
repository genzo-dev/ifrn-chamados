package com.ifrn.chamados.department.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentUpdateDTO(    
  @NotBlank(message = "O nome é obrigatório")
  String name,

  @NotBlank(message = "A sigla é obrigatória")
  String abbreviation
) {}
