package com.ifrn.chamados.category;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ifrn.chamados.category.dto.CategoryCreateDTO;
import com.ifrn.chamados.category.dto.CategoryResponseDTO;
import com.ifrn.chamados.category.dto.CategoryUpdateDTO;

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

  @GetMapping
  @ApiResponse(
    responseCode = "200", 
    description = "Categoria(s) encontradas/filtradas", 
    content = @Content(schema = @Schema(hidden = true))
  )
  public ResponseEntity<Page<CategoryResponseDTO>> list(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long departmentId,
      @ParameterObject 
      @PageableDefault(size = 10, sort = "name") Pageable pageable) {
    Page<Category> page = categoryService.findAllFiltered(name, departmentId, pageable);
    return ResponseEntity.ok(page.map(CategoryResponseDTO::fromEntity));
  }

  @GetMapping("{id}")
  @ApiResponse(
    responseCode = "200", 
    description = "Categoria encontrada com sucesso", 
    content = @Content(schema = @Schema(hidden = true))
  )
  @ApiResponse(
    responseCode = "404", 
    description = "Categoria não encontrada", 
    content = @Content(schema = @Schema(hidden = true))
  )
  public ResponseEntity<CategoryResponseDTO> findById(@PathVariable Long id) {
    Category searched = categoryService.findById(id);
    return ResponseEntity.ok(CategoryResponseDTO.fromEntity(searched));
  }

  @PutMapping("{id}")
  @ApiResponse(
    responseCode = "200", 
    description = "Categoria atualizada com sucesso", 
    content = @Content(schema = @Schema(hidden = true))
  )
  @ApiResponse(
    responseCode = "404", 
    description = "Departamento ou Categoria não encontrado", 
    content = @Content(schema = @Schema(hidden = true))
  )
  public ResponseEntity<CategoryResponseDTO> update(@PathVariable Long id, @RequestBody @Valid CategoryUpdateDTO dto) {
    Category updated = categoryService.updateById(id, dto);
    return ResponseEntity.ok(CategoryResponseDTO.fromEntity(updated));
  }

  @DeleteMapping("{id}")
  @ApiResponse(
    responseCode = "200", 
    description = "Categoria deletada com sucesso", 
    content = @Content(schema = @Schema(hidden = true))
  )
  @ApiResponse(
    responseCode = "404", 
    description = "Categoria não encontrada", 
    content = @Content(schema = @Schema(hidden = true))
  )
  @ApiResponse(
    responseCode = "409", 
    description = "Existe algum chamado vinculado à categoria em questão",
    content = @Content(schema = @Schema(hidden = true))
  )
  public ResponseEntity<CategoryResponseDTO> delete(@PathVariable Long id) {
    Category searched = categoryService.deleteById(id);
    return ResponseEntity.ok(CategoryResponseDTO.fromEntity(searched));
  }
}
