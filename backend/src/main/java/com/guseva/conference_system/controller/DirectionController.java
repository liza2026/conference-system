package com.guseva.conference_system.controller;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guseva.conference_system.dto.DirectionResponse;
import com.guseva.conference_system.repository.DirectionRepository;

/**
 * Справочник направлений
 */
@RestController
@RequestMapping("/api/directions")
public class DirectionController {

    private final DirectionRepository directions;

    public DirectionController(DirectionRepository directions) {
        this.directions = directions;
    }

    @GetMapping
    public List<DirectionResponse> list() {
        return directions.findAll(Sort.by("id")).stream()
                .map(d -> new DirectionResponse(d.getId(), d.getName()))
                .toList();
    }
}