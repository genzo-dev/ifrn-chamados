package com.ifrn.chamados.users.dto;

import com.ifrn.chamados.common.enums.UserType;
import com.ifrn.chamados.users.Users;

public record UserResponseDTO(
    Long id,
    String name,
    String email,
    String registrationNumber,
    UserType userType,
    Long departmentId
) {
    public static UserResponseDTO fromEntity(Users user) {
        return new UserResponseDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRegistrationNumber(),
            user.getUserType(),
            user.getDepartment() != null ? user.getDepartment().getId() : null
        );
    }
}