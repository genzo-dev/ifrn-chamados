package com.ifrn.chamados.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  boolean existsByNameIgnoreCase(String name);
  boolean existsByDescriptionIgnoreCase(String description);
  Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
  Page<Category> findByDepartmentId(Long departmentId, Pageable pageable);
  Page<Category> findByNameContainingIgnoreCaseAndDepartmentId(String name, Long departmentId, Pageable pageable);


}
