package com.ifrn.chamados.department;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ifrn.chamados.category.Category;
import com.ifrn.chamados.category.CategoryRepository;
import com.ifrn.chamados.common.exception.DuplicateResourceException;
import com.ifrn.chamados.common.exception.ResourceInUseException;
import com.ifrn.chamados.common.utils.EntityFinder;
import com.ifrn.chamados.department.dto.DepartmentRequestDTO;
import com.ifrn.chamados.department.dto.DepartmentUpdateDTO;
import com.ifrn.chamados.ticket.TicketRepository;
import com.ifrn.chamados.users.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentService {
  private final DepartmentRepository departmentRepository;
  private final TicketRepository ticketRepository;
  private final CategoryRepository categoryRepository;
  private final UsersRepository usersRepository;
  

  public Department create(DepartmentRequestDTO dto) {
    if (departmentRepository.existsByNameIgnoreCase(dto.name())) {
      throw new DuplicateResourceException("Já existe um departamento com esse nome.");
    }

    if (departmentRepository.existsByAbbreviationIgnoreCase(dto.abbreviation())) {
      throw new DuplicateResourceException("Já existe um departamento com essa sigla.");
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
    return EntityFinder.findByIdOrThrow(departmentRepository, id, "Departamento");
  }

  @Transactional 
  public Department updateById(Long id, DepartmentUpdateDTO dto) {
    Department department = EntityFinder.findByIdOrThrow(departmentRepository, id, "Departamento");

    if (departmentRepository.existsByNameIgnoreCaseAndIdNot(dto.name(), id)) {
      throw new DuplicateResourceException("Já existe um departamento com esse nome.");
    }

    if (departmentRepository.existsByAbbreviationIgnoreCaseAndIdNot(dto.abbreviation(), id)) {
      throw new DuplicateResourceException("Já existe um departamento com essa sigla.");
    }

    department.setName(dto.name());
    department.setAbbreviation(dto.abbreviation());

    return departmentRepository.save(department);
  }

  @Transactional 
  public Department deleteById(Long id) {
  Department department = EntityFinder.findByIdOrThrow(departmentRepository, id, "Departamento");

  if (ticketRepository.existsByCategoryDepartmentId(id)) {
    throw new ResourceInUseException(
        "Não é possível excluir um setor que possui chamados vinculados."
    );
  }

  if (categoryRepository.existsByDepartmentId(id)) {
    throw new ResourceInUseException(
        "Não é possível excluir um setor que possui categorias vinculadas."
    );
  }

  if (usersRepository.existsByDepartmentId(id)) {
    throw new ResourceInUseException(
        "Não é possível excluir um setor que possui usuários vinculados."
    );
  }

  departmentRepository.delete(department);
  return department;
}

  public List<Category> findCategoriesByDepartment(Long id) {
      Department department = EntityFinder.findByIdOrThrow(departmentRepository, id, "Departamento");
      return department.getCategories();
  }
}
