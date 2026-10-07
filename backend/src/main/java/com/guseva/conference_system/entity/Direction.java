package com.guseva.conference_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "direction")
public class Direction {

    @Id
    private Short id;

    @Column(nullable = false)
    private String name;

    protected Direction() {
    }

    public Short getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}