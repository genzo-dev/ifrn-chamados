package com.ifrn.chamados.interaction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import com.ifrn.chamados.ticket.Ticket;
import com.ifrn.chamados.users.Users;

@Entity
@Getter
@Setter 
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private LocalDateTime date_time;

    @ManyToOne 
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "author_id") 
    private Users user;
}
