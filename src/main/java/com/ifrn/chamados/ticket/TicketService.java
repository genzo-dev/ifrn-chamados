package com.ifrn.chamados.ticket;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ifrn.chamados.category.Category;
import com.ifrn.chamados.category.CategoryRepository;
import com.ifrn.chamados.common.enums.Priority;
import com.ifrn.chamados.common.enums.Status;
import com.ifrn.chamados.common.enums.UserType;
import com.ifrn.chamados.common.utils.EntityFinder;
import com.ifrn.chamados.ticket.dto.TicketCreateDTO;
import com.ifrn.chamados.users.Users;
import com.ifrn.chamados.users.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UsersRepository usersRepository;
    private final CategoryRepository categoryRepository;

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
}
