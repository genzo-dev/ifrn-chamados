package com.ifrn.chamados.ticket.dto;

import com.ifrn.chamados.common.enums.Priority;
import com.ifrn.chamados.common.enums.Status;
import com.ifrn.chamados.ticket.Ticket;

import java.time.LocalDateTime;

public record TicketResponseDTO(
    Long id,
    String title,
    String description,
    String location,
    Priority priority,
    Status status,
    LocalDateTime openingDate,
    LocalDateTime closingDate,
    String solution,
    Long requesterId,
    String requesterName,
    Long categoryId,
    String categoryName,
    Long technicianId,
    String technicianName
) {
    public static TicketResponseDTO fromEntity(Ticket ticket) {
        return new TicketResponseDTO(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getLocation(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.getOpeningDate(),
            ticket.getClosingDate(),
            ticket.getSolution(),
            ticket.getRequester().getId(),
            ticket.getRequester().getName(),
            ticket.getCategory().getId(),
            ticket.getCategory().getName(),
            ticket.getTechnician() != null ? ticket.getTechnician().getId() : null,
            ticket.getTechnician() != null ? ticket.getTechnician().getName() : null
        );
    }
}