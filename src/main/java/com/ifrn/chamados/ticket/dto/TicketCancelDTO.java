package com.ifrn.chamados.ticket.dto;

import jakarta.validation.constraints.NotNull;

public record TicketCancelDTO(
    @NotNull(message = "O ID do solicitante é obrigatório")
    Long requesterId
) {}