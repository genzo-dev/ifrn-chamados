package com.ifrn.chamados.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter 
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String abbreviation;

    @OneToMany(mappedBy = "department")
    private List<Category> categories;

    @OneToMany(mappedBy = "department")
    private List<Users> technicians;

}

