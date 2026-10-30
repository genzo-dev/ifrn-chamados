package com.ifrn.chamados.common.utils;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ifrn.chamados.common.exception.ResourceNotFoundException;

public class EntityFinder {
  public static <T> T findByIdOrThrow(JpaRepository<T, Long> repository, Long id, String entityName) {
      return repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(entityName, id));
  }
}
