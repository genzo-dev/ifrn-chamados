package com.ifrn.chamados.category;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifrn.chamados.category.dto.CategoryCreateDTO;
import com.ifrn.chamados.category.dto.CategoryResponseDTO;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/category")
@RequiredArgsConstructor 
public class CategoryController {

  private final CategoryService categoryService;

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
  public ResponseEntity<CategoryResponseDTO> create(@RequestBody @Valid CategoryCreateDTO dto) {
    Category created = categoryService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponseDTO.fromEntity(created));
  }

}
