package com.guseva.conference_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guseva.conference_system.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}