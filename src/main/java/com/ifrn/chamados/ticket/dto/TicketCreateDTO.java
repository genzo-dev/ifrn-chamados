package com.ifrn.chamados.ticket.dto;

import com.ifrn.chamados.common.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketCreateDTO(
    @NotBlank(message = "O título do chamado é obrigatório")
    String title,

    @NotBlank(message = "A descrição do chamado é obrigatória")
    String description,

    @NotBlank(message = "O local é obrigatório")
    String location,

    @NotNull(message = "O ID do solicitante é obrigatório")
    Long requesterId,

    @NotNull(message = "O ID da categoria é obrigatório")
    Long categoryId,

    Priority priority
) {}