package com.ifrn.chamados.interaction.dto;

import com.ifrn.chamados.interaction.Interaction;
import java.time.LocalDateTime;

public record InteractionResponseDTO(
    Long id,
    String text,
    LocalDateTime dateTime,
    Long authorId,
    String authorName
) {
    public static InteractionResponseDTO fromEntity(Interaction interaction) {
        return new InteractionResponseDTO(
            interaction.getId(),
            interaction.getText(),
            interaction.getDateTime(),
            interaction.getUser().getId(),
            interaction.getUser().getName()
        );
    }
}