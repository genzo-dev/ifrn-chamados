package com.ifrn.chamados.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import com.ifrn.chamados.enums.Priority;
import com.ifrn.chamados.enums.Status;

@Entity
@Getter
@Setter 
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String location;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime openingDate;
    private LocalDateTime closingDate;
    private String solution;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private Users requester;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "technician_id") 
    private Users technician;
}
