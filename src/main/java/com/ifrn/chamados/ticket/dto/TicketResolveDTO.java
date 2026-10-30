package com.ifrn.chamados.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketResolveDTO(
    @NotNull(message = "O ID do técnico é obrigatório")
    Long technicianId,

    @NotBlank(message = "A descrição da solução é obrigatória")
    String solution
) {}