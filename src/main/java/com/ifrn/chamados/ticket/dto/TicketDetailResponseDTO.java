package com.ifrn.chamados.ticket.dto;


import com.ifrn.chamados.common.enums.Priority;
import com.ifrn.chamados.common.enums.Status;
import com.ifrn.chamados.interaction.Interaction;
import com.ifrn.chamados.interaction.dto.InteractionResponseDTO;
import com.ifrn.chamados.ticket.Ticket;

import java.time.LocalDateTime;
import java.util.List;

public record TicketDetailResponseDTO(
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
    String technicianName,
    List<InteractionResponseDTO> interactions
) {
    public static TicketDetailResponseDTO fromEntity(Ticket ticket, List<Interaction> interactions) {
        List<InteractionResponseDTO> interactionDTOs = interactions.stream()
                .map(InteractionResponseDTO::fromEntity)
                .toList();

        return new TicketDetailResponseDTO(
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
            ticket.getTechnician() != null ? ticket.getTechnician().getName() : null,
            interactionDTOs
        );
    }
}