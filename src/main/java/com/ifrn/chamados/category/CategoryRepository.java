package com.ifrn.chamados.category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  boolean existsByNameIgnoreCase(String name);
  boolean existsByDescriptionIgnoreCase(String description);
}
