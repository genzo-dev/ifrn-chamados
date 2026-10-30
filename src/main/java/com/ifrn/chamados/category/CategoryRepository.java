package com.ifrn.chamados.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  boolean existsByNameIgnoreCase(String name);
  boolean existsByDescriptionIgnoreCase(String description);
  @Query("""
      SELECT c FROM Category c
      WHERE (:name IS NULL OR :name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:departmentId IS NULL OR c.department.id = :departmentId)
  """)
  Page<Category> findWithFilters(
      @Param("name") String name,
      @Param("departmentId") Long departmentId,
      Pageable pageable
  );
}
