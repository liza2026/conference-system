package com.guseva.conference_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "supervisor")
public class Supervisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    /** Порядковый номер руководителя в заявке: 1 или 2. */
    @Column(nullable = false)
    private short position;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    /** Должность и кафедра, например «доцент каф. АПП». */
    @Column(nullable = false)
    private String post;

    protected Supervisor() {
    }

    Supervisor(Application application, int position, String fullName, String post) {
        this.application = application;
        this.position = (short) position;
        update(fullName, post);
    }

    public void update(String fullName, String post) {
        this.fullName = fullName;
        this.post = post;
    }

    public Long getId() { return id; }
    public short getPosition() { return position; }
    public String getFullName() { return fullName; }
    public String getPost() { return post; }
}