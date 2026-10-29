package com.ifrn.chamados.category;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import com.ifrn.chamados.category.dto.CategoryCreateDTO;
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
}

