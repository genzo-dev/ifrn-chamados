package com.ifrn.chamados.department;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
  Page<Department> findByNameContainingIgnoreCase(String name, Pageable pageable);
  boolean existsByNameIgnoreCase(String name);
  boolean existsByAbbreviationIgnoreCase(String abbreviation);
  boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
  boolean existsByAbbreviationIgnoreCaseAndIdNot(String abbreviation, Long id);
}
