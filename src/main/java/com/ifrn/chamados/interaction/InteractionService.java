package com.ifrn.chamados.interaction;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ifrn.chamados.common.enums.Status;
import com.ifrn.chamados.common.utils.EntityFinder;
import com.ifrn.chamados.interaction.dto.InteractionCreateDTO;
import com.ifrn.chamados.ticket.Ticket;
import com.ifrn.chamados.ticket.TicketRepository;
import com.ifrn.chamados.users.Users;
import com.ifrn.chamados.users.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class InteractionService {
    private final InteractionRepository interactionRepository;
    private final TicketRepository ticketRepository;
    private final UsersRepository usersRepository;


        public Interaction create(Long ticketId, InteractionCreateDTO dto) {
        Ticket ticket = EntityFinder.findByIdOrThrow(ticketRepository, ticketId, "Chamado");
        
        if (ticket.getStatus() == Status.CANCELADO || ticket.getStatus() ==  Status.RESOLVIDO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível registrar interações em chamados resolvidos ou cancelados.");
        }

        Users author = EntityFinder.findByIdOrThrow(usersRepository, dto.authorId(), "Usuário autor");

        boolean isRequester = ticket.getRequester().getId().equals(author.getId());
        boolean isAssignedTechnician = ticket.getTechnician() != null && ticket.getTechnician().getId().equals(author.getId());

        if (!isRequester && !isAssignedTechnician) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Apenas o solicitante ou o técnico responsável pelo chamado podem registrar interações.");
        }

        Interaction interaction = new Interaction();
        interaction.setText(dto.text());
        interaction.setDateTime(LocalDateTime.now());
        interaction.setTicket(ticket);
        interaction.setUser(author);

        return interactionRepository.save(interaction);
    }
}
