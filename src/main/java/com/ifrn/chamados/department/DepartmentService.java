package com.ifrn.chamados.department;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ifrn.chamados.category.Category;
import com.ifrn.chamados.department.dto.DepartmentRequestDTO;
import com.ifrn.chamados.department.dto.DepartmentUpdateDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentService {
  private final DepartmentRepository departmentRepository;

  public Department create(DepartmentRequestDTO dto) {
    if (departmentRepository.existsByNameIgnoreCase(dto.name())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um departamento com esse nome.");
    }

    if (departmentRepository.existsByAbbreviationIgnoreCase(dto.abbreviation())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um departamento com essa sigla.");
    }

    Department department = new Department();
    department.setName(dto.name());
    department.setAbbreviation(dto.abbreviation());
    return departmentRepository.save(department);
  }

  public Page<Department> findAll(String name, Pageable pageable) {
    if (name != null && !name.isBlank()) {
      return departmentRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    return departmentRepository.findAll(pageable);
  }

  public Department findById(Long id) {
    return departmentRepository.findById(id).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departamento com ID " + id + " não encontrado."));
  }

  public Department updateById(Long id, DepartmentUpdateDTO dto) {
    Department department = departmentRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departamento com ID " + id + " não encontrado"));

    if (dto.name() != null && !dto.name().isBlank()) {
      department.setName(dto.name());
    }

    if (dto.abbreviation() != null && !dto.abbreviation().isBlank()) {
      department.setAbbreviation(dto.abbreviation());;
    }

    return departmentRepository.save(department);
  }

  public Department deleteById(Long id) {
    // TODO: adicionar regra que impeça exclusão em caso de chamado (ticket) vinculado 

    Department department = departmentRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departamento com ID " + id + " não encontrado"));

    departmentRepository.delete(department);

    return department;
  }

  public List<Category> findCategoriesByDepartment(Long id) {
      Department department = departmentRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departamento com ID " + id + " não encontrado"));

      return department.getCategories();
  }
}
