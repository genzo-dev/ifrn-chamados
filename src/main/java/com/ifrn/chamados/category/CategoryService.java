package com.ifrn.chamados.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import com.ifrn.chamados.category.dto.CategoryCreateDTO;
import com.ifrn.chamados.category.dto.CategoryUpdateDTO;
import com.ifrn.chamados.department.Department;
import com.ifrn.chamados.department.DepartmentRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;

    public Category create(CategoryCreateDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A categoria " + dto.name() + " já existe.");
        }

        if (categoryRepository.existsByDescriptionIgnoreCase(dto.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com a mesma descrição.");
        }

        Department department = departmentRepository.findById(dto.department_id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departamento não encontrado"));

        Category category = new Category();
        category.setName(dto.name());
        category.setDescription(dto.description());
        category.setDepartment(department);

        return categoryRepository.save(category);
    }

    public Page<Category> findAllFiltered(String name, Long departmentId, Pageable pageable) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasDepartmentId = departmentId != null;

        if (hasName && hasDepartmentId) {
            return categoryRepository.findByNameContainingIgnoreCaseAndDepartmentId(name, departmentId, pageable);
        } else if (hasName) {
            return categoryRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (hasDepartmentId) {
            return categoryRepository.findByDepartmentId(departmentId, pageable);
        }

        return categoryRepository.findAll(pageable);
    }

    public Category findById(Long id) {
    return categoryRepository.findById(id).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria com ID " + id + " não encontrado."));
  }

    public Category updateById(Long id, CategoryUpdateDTO dto) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria com ID " + id + " não encontrada"));

        if (dto.name() != null && !dto.name().isBlank()) {
            category.setName(dto.name());
        }

        if (dto.description() != null && !dto.description().isBlank()) {
            category.setDescription(dto.description());;
        }

        if (dto.departmentId() != null) {
            Department department = departmentRepository.findById(dto.departmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departamento não encontrado"));
            category.setDepartment(department);
        }

        return categoryRepository.save(category);
  }

    public Category deleteById(Long id) {
        // TODO: adicionar regra que impeça exclusão em caso de chamado (ticket) vinculado 

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria com ID " + id + " não encontrada"));

        categoryRepository.delete(category);

        return category;
  }
}

