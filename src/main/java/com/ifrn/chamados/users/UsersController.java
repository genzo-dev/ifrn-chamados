package com.ifrn.chamados.users;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ifrn.chamados.category.dto.CategoryResponseDTO;
import com.ifrn.chamados.common.enums.UserType;
import com.ifrn.chamados.users.dto.UserCreateDTO;
import com.ifrn.chamados.users.dto.UserResponseDTO;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor 
public class UsersController {
    private final UsersService usersService;

    @PostMapping
    @ApiResponse(
        responseCode = "201", 
        description = "Usuário criado com sucesso", 
        content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
        responseCode = "400", 
        description = "Dados inválidos ou fornecidos incorretamente", 
        content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
        responseCode = "409", 
        description = "Já existe um usuário com esse e-mail", 
        content = @Content(schema = @Schema(hidden = true))
    )
    public ResponseEntity<UserResponseDTO> create(@RequestBody @Valid UserCreateDTO dto) {
        Users created = usersService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDTO.fromEntity(created));
    }

    @GetMapping
    @ApiResponse(
        responseCode = "200", 
        description = "Usuário(s) encontrados/filtrados", 
        content = @Content(schema = @Schema(hidden = true))
    )
    public ResponseEntity<Page<UserResponseDTO>> list(
        @RequestParam(required = false, name = "nome") String name,
        @RequestParam(required = false, name = "tipo") UserType userType,
        @RequestParam(required = false, name = "setorId") Long departmentId,
        @ParameterObject 
        @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<Users> usersPage = usersService.findAllFiltered(name, userType, departmentId, pageable);
        return ResponseEntity.ok(usersPage.map(UserResponseDTO::fromEntity));
    }

    @GetMapping("{id}")
    @ApiResponse(
        responseCode = "200", 
        description = "Usuário encontrado com sucesso", 
        content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Usuário não encontrado", 
        content = @Content(schema = @Schema(hidden = true))
    )
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        Users searched = usersService.findById(id);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(searched));
    }
}
