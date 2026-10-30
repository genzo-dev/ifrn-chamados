package com.ifrn.chamados.ticket.dto;

import jakarta.validation.constraints.NotNull;

public record TicketAssignDTO(
    @NotNull(message = "O ID do técnico é obrigatório")
    Long technicianId
) {}