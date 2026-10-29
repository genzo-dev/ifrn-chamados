package com.ifrn.chamados.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  boolean existsByNameIgnoreCase(String name);
  boolean existsByDescriptionIgnoreCase(String description);
  // TODO: Alterar todos os Page<> para utilizar @Query e tornar o código mais simples, além de diminuir os blocos de if/else do service
  Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
  Page<Category> findByDepartmentId(Long departmentId, Pageable pageable);
  Page<Category> findByNameContainingIgnoreCaseAndDepartmentId(String name, Long departmentId, Pageable pageable);

}
