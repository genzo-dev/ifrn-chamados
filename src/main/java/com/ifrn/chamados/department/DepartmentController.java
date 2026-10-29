package com.ifrn.chamados.department;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifrn.chamados.department.dto.DepartmentRequestDTO;
import com.ifrn.chamados.department.dto.DepartmentResponseDTO;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentController {
  private final DepartmentService departmentService;

  @PostMapping
  @ApiResponse(
    responseCode = "201", 
    description = "Departamento criado com sucesso", 
    content = @Content(schema = @Schema(hidden = true))
  )
  @ApiResponse(
    responseCode = "400", 
    description = "Dados inválidos ou fornecidos incorretamente", 
    content = @Content(schema = @Schema(hidden = true))
  )
  @ApiResponse(
    responseCode = "409", 
    description = "Já existe um departamento com o nome ou a sigla informada", 
    content = @Content(schema = @Schema(hidden = true))
  )
  public ResponseEntity<DepartmentResponseDTO> create(@RequestBody @Valid DepartmentRequestDTO dto) {
    Department created = departmentService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(DepartmentResponseDTO.fromEntity(created));
  }

  @GetMapping
  @ApiResponse(
    responseCode = "200", 
    description = "Departamento(s) encontrados/filtrados", 
    content = @Content(schema = @Schema(hidden = true))
  )
  public ResponseEntity<Page<DepartmentResponseDTO>> list(
      @RequestParam(required = false) String nome,
      @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {
    Page<Department> page = departmentService.findAll(nome, pageable);
    return ResponseEntity.ok(page.map(DepartmentResponseDTO::fromEntity));
  }

  @GetMapping("{id}")
  @ApiResponse(
    responseCode = "200", 
    description = "Departamento encontrado com sucesso", 
    content = @Content(schema = @Schema(hidden = true))
  )
  @ApiResponse(
    responseCode = "404", 
    description = "Departamento não encontrado", 
    content = @Content(schema = @Schema(hidden = true))
  )
  public ResponseEntity<DepartmentResponseDTO> findById(@PathVariable Long id) {
    Department searched = departmentService.findById(id);
    return ResponseEntity.ok(DepartmentResponseDTO.fromEntity(searched));
  }

  @DeleteMapping("{id}")
  @ApiResponse(
    responseCode = "200", 
    description = "Departamento deletado com sucesso", 
    content = @Content(schema = @Schema(hidden = true))
  )
  @ApiResponse(
    responseCode = "404", 
    description = "Departamento não encontrado", 
    content = @Content(schema = @Schema(hidden = true))
  )
  @ApiResponse(
    responseCode = "409", 
    description = "Existe algum chamado vinculado ao departamento em questão",
    content = @Content(schema = @Schema(hidden = true))
  )
  public ResponseEntity<DepartmentResponseDTO> delete(@PathVariable Long id) {
    Department searched = departmentService.deleteById(id);
    return ResponseEntity.ok(DepartmentResponseDTO.fromEntity(searched));
  }
}
