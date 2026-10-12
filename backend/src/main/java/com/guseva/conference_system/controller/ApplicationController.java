package com.guseva.conference_system.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import com.guseva.conference_system.util.FileNames;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.guseva.conference_system.dto.ApplicationRequest;
import com.guseva.conference_system.dto.ApplicationResponse;
import com.guseva.conference_system.entity.Application;
import com.guseva.conference_system.mapper.ApplicationMapper;
import com.guseva.conference_system.service.ApplicationService;

import jakarta.validation.Valid;

/**
 * REST-интерфейс заявок
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService service;
    private final ApplicationMapper mapper;
    /** Форматы, в которых принимаются тезисы */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("doc", "docx");

    public ApplicationController(ApplicationService service, ApplicationMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /** Подать заявку
     * "data" - JSON с данными
     * "file" - файл тезисов
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse submit(@RequestPart("data") @Valid ApplicationRequest data,
                                      @RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл тезисов пуст");
        }
        if (!ALLOWED_EXTENSIONS.contains(FileNames.extension(file.getOriginalFilename()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Тезисы принимаются только в форматах .doc и .docx");
        }
        return mapper.toResponse(service.submit(data, file.getOriginalFilename(), file.getBytes()));
    }

    /** Изменить заявку */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApplicationResponse update(@PathVariable Long id,
                                      @RequestPart("data") @Valid ApplicationRequest data,
                                      @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        boolean hasFile = file != null && !file.isEmpty();
        if (hasFile && !ALLOWED_EXTENSIONS.contains(FileNames.extension(file.getOriginalFilename()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Тезисы принимаются только в форматах .doc и .docx");
        }
        return mapper.toResponse(service.update(id, data,
                hasFile ? file.getOriginalFilename() : null,
                hasFile ? file.getBytes() : null));
    }

    /** Отозвать заявку */
    @PostMapping("/{id}/withdraw")
    public ApplicationResponse withdraw(@PathVariable Long id) {
        return mapper.toResponse(service.withdraw(id));
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable Long id) {
        return mapper.toResponse(service.get(id));
    }

    @GetMapping
    public List<ApplicationResponse> list() {
        return service.list().stream().map(mapper::toResponse).toList();
    }

    /** Скачать файл тезисов под именем по шаблону из письма */
    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        Application application = service.get(id);
        String disposition = ContentDisposition.attachment()
                .filename(mapper.thesisFileName(application), StandardCharsets.UTF_8)
                .build().toString();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(application.getThesisFile());
    }
}