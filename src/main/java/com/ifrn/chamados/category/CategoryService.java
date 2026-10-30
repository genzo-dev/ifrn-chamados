package com.ifrn.chamados.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import com.ifrn.chamados.category.dto.CategoryCreateDTO;
import com.ifrn.chamados.category.dto.CategoryUpdateDTO;
import com.ifrn.chamados.common.utils.EntityFinder;
import com.ifrn.chamados.department.Department;
import com.ifrn.chamados.department.DepartmentRepository;
import com.ifrn.chamados.ticket.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final TicketRepository ticketRepository;

    public Category create(CategoryCreateDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A categoria " + dto.name() + " já existe.");
        }

        if (categoryRepository.existsByDescriptionIgnoreCase(dto.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com a mesma descrição.");
        }

        Department department = EntityFinder.findByIdOrThrow(departmentRepository, dto.department_id(), "Departamento");

        Category category = new Category();
        category.setName(dto.name());
        category.setDescription(dto.description());
        category.setDepartment(department);

        return categoryRepository.save(category);
    }

    public Page<Category> findAllFiltered(String name, Long departmentId, Pageable pageable) {
        return categoryRepository.findWithFilters(name, departmentId, pageable);
    }

    public Category findById(Long id) {
        return EntityFinder.findByIdOrThrow(categoryRepository, id, "Categoria");
    }

    public Category updateById(Long id, CategoryUpdateDTO dto) {
        Category category = EntityFinder.findByIdOrThrow(categoryRepository, id, "Categoria");

        if (dto.name() != null && !dto.name().isBlank()) {
            category.setName(dto.name());
        }

        if (dto.description() != null && !dto.description().isBlank()) {
            category.setDescription(dto.description());;
        }

        if (dto.departmentId() != null) {
            Department department = EntityFinder.findByIdOrThrow(departmentRepository, id, "Departamento");
            category.setDepartment(department);
        }

        return categoryRepository.save(category);
  }

    public Category deleteById(Long id) {
        Category category = EntityFinder.findByIdOrThrow(categoryRepository, id, "Categoria");

        if (ticketRepository.existsByCategoryId(id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                "Não é possível excluir uma categoria que possui chamados vinculados."
            );
        }

        categoryRepository.delete(category);

        return category;
  }
}

