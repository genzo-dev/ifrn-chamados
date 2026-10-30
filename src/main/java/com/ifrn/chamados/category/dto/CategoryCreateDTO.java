package com.ifrn.chamados.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryCreateDTO(
    @NotBlank(message = "O nome é obrigatório") 
    String name,

    @NotBlank(message = "A descrição é obrigatória") 
    String description,

    @NotNull (message = "A categoria deve ser vinculada a algum departamento existente")
    Long department_id
) {}
