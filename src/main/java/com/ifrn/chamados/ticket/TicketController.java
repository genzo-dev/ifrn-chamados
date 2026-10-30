package com.ifrn.chamados.ticket;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifrn.chamados.ticket.dto.TicketCreateDTO;
import com.ifrn.chamados.ticket.dto.TicketResponseDTO;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @PostMapping
    @ApiResponse(
        responseCode = "201", 
        description = "Chamado criado com sucesso", 
        content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
        responseCode = "400", 
        description = "Dados inválidos ou fornecidos incorretamente", 
        content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
        responseCode = "404", 
        description = "Usuário SOLICITANTE ou Categoria não encontrado", 
        content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
        responseCode = "409", 
        description = "Apenas usuários do tipo SOLICITANTE podem abrir chamados", 
        content = @Content(schema = @Schema(hidden = true))
    )
    public ResponseEntity<TicketResponseDTO> create(@RequestBody @Valid TicketCreateDTO dto) {
        Ticket created = ticketService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(TicketResponseDTO.fromEntity(created));
    }
}
