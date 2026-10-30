package com.ifrn.chamados.ticket;

import java.time.LocalDateTime;
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
import com.ifrn.chamados.ticket.dto.TicketAssignDTO;
import com.ifrn.chamados.ticket.dto.TicketCancelDTO;
import com.ifrn.chamados.ticket.dto.TicketCreateDTO;
import com.ifrn.chamados.ticket.dto.TicketDetailResponseDTO;
import com.ifrn.chamados.ticket.dto.TicketResolveDTO;
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

    public Page<Ticket> findByUserIdAndStatus(Long userId, Status status, Pageable pageable) {
        EntityFinder.findByIdOrThrow(usersRepository, userId, "Usuário");

        return ticketRepository.findByUserIdAndStatus(userId, status, pageable);
    }

    public Ticket assignTicket(Long ticketId, TicketAssignDTO dto) {
        Ticket ticket = EntityFinder.findByIdOrThrow(ticketRepository, ticketId, "Chamado");

        if (ticket.getStatus() != Status.ABERTO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Apenas chamados com status ABERTO podem ser assumidos.");
        }

        Users technician = EntityFinder.findByIdOrThrow(usersRepository, dto.technicianId(), "Técnico");

        if (technician.getUserType() != UserType.TECNICO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "O usuário informado não é um técnico.");
        }

        Long categoryDepartmentId = ticket.getCategory().getDepartment().getId();
        boolean belongsToDepartment = technician.getDepartment() != null 
                && technician.getDepartment().getId().equals(categoryDepartmentId);

        if (!belongsToDepartment) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "O técnico não pertence ao setor responsável pela categoria deste chamado.");
        }

        ticket.setTechnician(technician);
        ticket.setStatus(Status.EM_ANDAMENTO);

        return ticketRepository.save(ticket);
    }

    public Ticket resolveTicket(Long ticketId, TicketResolveDTO dto) {
        Ticket ticket = EntityFinder.findByIdOrThrow(ticketRepository, ticketId, "Chamado");

        if (ticket.getStatus() == Status.RESOLVIDO || ticket.getStatus() == Status.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Não é possível alterar chamados já resolvidos ou cancelados.");
        }

        if (ticket.getStatus() != Status.EM_ANDAMENTO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Apenas chamados com status EM_ATENDIMENTO podem ser resolvidos.");
        }

        Users technician = EntityFinder.findByIdOrThrow(usersRepository, dto.technicianId(), "Técnico");

        if (ticket.getTechnician() == null || !ticket.getTechnician().getId().equals(technician.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Apenas o técnico responsável por este chamado pode resolvê-lo.");
        }

        ticket.setSolution(dto.solution());
        ticket.setClosingDate(LocalDateTime.now());
        ticket.setStatus(Status.RESOLVIDO);

        return ticketRepository.save(ticket);
    }

    public Ticket cancelTicket(Long ticketId, TicketCancelDTO dto) {
        Ticket ticket = EntityFinder.findByIdOrThrow(ticketRepository, ticketId, "Chamado");

        if (ticket.getStatus() != Status.ABERTO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Apenas chamados com status ABERTO podem ser cancelados.");
        }

        Users requester = EntityFinder.findByIdOrThrow(usersRepository, dto.requesterId(), "Solicitante");

        if (!ticket.getRequester().getId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Apenas o próprio solicitante pode cancelar o chamado.");
        }

        ticket.setStatus(Status.CANCELADO);

        return ticketRepository.save(ticket);
    }
}
