package com.ifrn.chamados.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InteractionCreateDTO(
    @NotBlank(message = "O texto da interação é obrigatório")
    String text,

    @NotNull (message = "O ID do autor é obrigatório")
    Long authorId
) {}
