package com.ifrn.chamados.ticket;

import java.time.LocalDateTime;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ifrn.chamados.common.enums.Priority;
import com.ifrn.chamados.common.enums.Status;
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

    @GetMapping
    @ApiResponse(
        responseCode = "200", 
        description = "Chamado(s) encontrados/filtrados", 
        content = @Content(schema = @Schema(hidden = true))
    )
    public ResponseEntity<Page<TicketResponseDTO>> list(
            @RequestParam(required = false, name = "status") Status status,
            @RequestParam(required = false, name = "prioridade") Priority priority,
            @RequestParam(required = false, name = "setorId") Long departmentId,
            @RequestParam(required = false, name = "categoriaId") Long categoryId,
            @RequestParam(required = false, name = "solicitanteId") Long requesterId,
            @RequestParam(required = false, name = "tecnicoId") Long technicianId,
            @RequestParam(required = false, name = "dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false, name = "dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @ParameterObject 
            @PageableDefault(size = 10, sort = "openingDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Ticket> page = ticketService.findAllFiltered(
            status, priority, departmentId, categoryId, requesterId, technicianId, dataInicio, dataFim, pageable
        );

        return ResponseEntity.ok(page.map(TicketResponseDTO::fromEntity));
    }
}
