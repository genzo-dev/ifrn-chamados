package com.ifrn.chamados.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.ifrn.chamados.enums.UserType;

@Entity
@Getter
@Setter 
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserType userType;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;
}

