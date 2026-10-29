package com.ifrn.chamados.users.dto;

import com.ifrn.chamados.common.enums.UserType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserUpdateDTO(
    @NotBlank(message = "O nome de usuário é obrigatório")
    String name,

    @NotBlank(message = "O e-mail do usuário é obrigatório")
    @Email(message = "O e-mail informado é inválido")
    String email,
    
    @NotNull (message = "O tipo do usuário é obrigatório")
    UserType userType,

    Long departmentId
) {} 
