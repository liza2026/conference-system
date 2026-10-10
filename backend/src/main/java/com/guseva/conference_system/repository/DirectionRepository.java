package com.guseva.conference_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guseva.conference_system.entity.Direction;

public interface DirectionRepository extends JpaRepository<Direction, Short> {
}