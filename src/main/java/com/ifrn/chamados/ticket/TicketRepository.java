package com.ifrn.chamados.ticket;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ifrn.chamados.common.enums.Priority;
import com.ifrn.chamados.common.enums.Status;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("""
        SELECT t FROM Ticket t
        WHERE (:status IS NULL OR t.status = :status)
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:departmentId IS NULL OR t.category.department.id = :departmentId)
          AND (:categoryId IS NULL OR t.category.id = :categoryId)
          AND (:requesterId IS NULL OR t.requester.id = :requesterId)
          AND (:technicianId IS NULL OR t.technician.id = :technicianId)
          AND (CAST(:dataInicio AS timestamp) IS NULL OR t.openingDate >= :dataInicio)
          AND (CAST(:dataFim AS timestamp) IS NULL OR t.openingDate <= :dataFim)
    """)
    Page<Ticket> findWithFilters(
        @Param("status") Status status,
        @Param("priority") Priority priority,
        @Param("departmentId") Long departmentId,
        @Param("categoryId") Long categoryId,
        @Param("requesterId") Long requesterId,
        @Param("technicianId") Long technicianId,
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        Pageable pageable
    );

    @Query("""
        SELECT t FROM Ticket t
        WHERE (t.requester.id = :userId OR t.technician.id = :userId)
          AND (:status IS NULL OR t.status = :status)
    """)
    Page<Ticket> findByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") Status status,
        Pageable pageable
    );
}
