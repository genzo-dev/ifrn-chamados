package com.ifrn.chamados.interaction;

import com.ifrn.chamados.interaction.dto.InteractionCreateDTO;
import com.ifrn.chamados.interaction.dto.InteractionResponseDTO;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping("/{id}/interaction")
    @ApiResponse(
        responseCode = "201", 
        description = "Interação criada com sucesso", 
        content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
        responseCode = "400", 
        description = "Dados inválidos ou fornecidos incorretamente", 
        content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Autor da interação ou chamado não encontrado", 
        content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
        responseCode = "409", 
        description = "Apenas o solicitante ou o técnico responsável pelo chamado podem registrar interações", 
        content = @Content(schema = @Schema(hidden = true))
    )
    public ResponseEntity<InteractionResponseDTO> create(
            @PathVariable Long id,
            @RequestBody @Valid InteractionCreateDTO dto) {
        
        Interaction created = interactionService.create(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(InteractionResponseDTO.fromEntity(created));
    }
}