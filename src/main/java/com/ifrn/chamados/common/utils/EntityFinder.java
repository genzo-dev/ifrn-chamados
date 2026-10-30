package com.ifrn.chamados.common.utils;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EntityFinder {
  public static <T> T findByIdOrThrow(JpaRepository<T, Long> repository, Long id, String entityName) {
      return repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, entityName + " com ID " + id + " não encontrado"));
  }
}
