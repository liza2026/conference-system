package com.guseva.conference_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "direction_user_role",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_direction_user_role",
                        columnNames = {"user_id", "direction_id", "role"}
                )
        }
)
public class DirectionUserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "direction_id", nullable = false)
    private Direction direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DirectionRole role;

    protected DirectionUserRole() {
    }

    public DirectionUserRole(
            User user,
            Direction direction,
            DirectionRole role
    ) {
        this.user = user;
        this.direction = direction;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Direction getDirection() {
        return direction;
    }

    public DirectionRole getRole() {
        return role;
    }
}
