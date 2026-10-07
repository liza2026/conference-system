package com.guseva.conference_system.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** Заявка на участие в конкурсе. Все данные работы хранятся здесь один раз. */
@Entity
@Table(name = "application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direction_id")
    private Direction direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "thesis_file_name", nullable = false)
    private String thesisFileName;

    @Column(name = "thesis_file", nullable = false)
    private byte[] thesisFile;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private List<Author> authors = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private List<Supervisor> supervisors = new ArrayList<>();

    protected Application() {
    }

    /** Новая заявка сразу имеет статус «подана». */
    public Application(String title, Direction direction, String thesisFileName,
                       byte[] thesisFile, Instant now) {
        this.title = title;
        this.direction = direction;
        this.thesisFileName = thesisFileName;
        this.thesisFile = thesisFile;
        this.status = ApplicationStatus.SUBMITTED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void change(String title, Direction direction, Instant now) {
        this.title = title;
        this.direction = direction;
        this.updatedAt = now;
    }

    public void replaceThesisFile(String fileName, byte[] file) {
        this.thesisFileName = fileName;
        this.thesisFile = file;
    }

    public void withdraw(Instant now) {
        this.status = ApplicationStatus.WITHDRAWN;
        this.updatedAt = now;
    }

    /** Добавляет автора; его номер (1 или 2) выставляется по порядку. */
    public Author addAuthor(String fullName, String groupName, String phone,
                            String email, Funding funding) {
        Author author = new Author(this, authors.size() + 1, fullName, groupName, phone, email, funding);
        authors.add(author);
        return author;
    }

    public void removeLastAuthor() {
        authors.remove(authors.size() - 1);
    }

    public Supervisor addSupervisor(String fullName, String post) {
        Supervisor supervisor = new Supervisor(this, supervisors.size() + 1, fullName, post);
        supervisors.add(supervisor);
        return supervisor;
    }

    public void removeLastSupervisor() {
        supervisors.remove(supervisors.size() - 1);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Direction getDirection() { return direction; }
    public ApplicationStatus getStatus() { return status; }
    public String getThesisFileName() { return thesisFileName; }
    public byte[] getThesisFile() { return thesisFile; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Author> getAuthors() { return authors; }
    public List<Supervisor> getSupervisors() { return supervisors; }
}