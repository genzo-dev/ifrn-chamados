package com.ifrn.chamados.ticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ifrn.chamados.category.Category;
import com.ifrn.chamados.category.CategoryRepository;
import com.ifrn.chamados.common.enums.Priority;
import com.ifrn.chamados.common.enums.Status;
import com.ifrn.chamados.common.enums.UserType;
import com.ifrn.chamados.common.utils.EntityFinder;
import com.ifrn.chamados.interaction.Interaction;
import com.ifrn.chamados.interaction.InteractionRepository;
import com.ifrn.chamados.ticket.dto.TicketCreateDTO;
import com.ifrn.chamados.ticket.dto.TicketDetailResponseDTO;
import com.ifrn.chamados.users.Users;
import com.ifrn.chamados.users.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UsersRepository usersRepository;
    private final CategoryRepository categoryRepository;
    private final InteractionRepository interactionRepository;

    public Ticket create(TicketCreateDTO dto) {
        Users requester = EntityFinder.findByIdOrThrow(usersRepository, dto.requesterId(), "Usuário solicitante");

        if (requester.getUserType() != UserType.SOLICITANTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Apenas usuários do tipo SOLICITANTE podem abrir chamados.");
        }

        Category category = EntityFinder.findByIdOrThrow(categoryRepository, dto.categoryId(), "Categoria");

        Ticket ticket = new Ticket();
        ticket.setTitle(dto.title());
        ticket.setDescription(dto.description());
        ticket.setLocation(dto.location());
        ticket.setRequester(requester);
        ticket.setCategory(category);
        
        ticket.setStatus(Status.ABERTO);
        ticket.setOpeningDate(LocalDateTime.now());
        ticket.setPriority(dto.priority() != null ? dto.priority() : Priority.MEDIA);

        return ticketRepository.save(ticket);
    }

    public Page<Ticket> findAllFiltered(
            Status status,
            Priority priority,
            Long departmentId,
            Long categoryId,
            Long requesterId,
            Long technicianId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Pageable pageable) {

        return ticketRepository.findWithFilters(
            status,
            priority,
            departmentId,
            categoryId,
            requesterId,
            technicianId,
            dataInicio,
            dataFim,
            pageable
        );
    }

    public TicketDetailResponseDTO findById(Long id) {
        Ticket ticket = EntityFinder.findByIdOrThrow(ticketRepository, id, "Chamado");

        List<Interaction> interactions = interactionRepository.findByTicketId(id);

        return TicketDetailResponseDTO.fromEntity(ticket, interactions);

    }
}
