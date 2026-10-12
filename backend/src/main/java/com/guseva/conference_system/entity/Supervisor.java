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

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;   // необязательное

    /** Должность и кафедра, например «доцент каф. АПП». */
    @Column(nullable = false)
    private String post;

    protected Supervisor() {
    }

    Supervisor(Application application, int position, String lastName, String firstName,
               String middleName, String post) {
        this.application = application;
        this.position = (short) position;
        update(lastName, firstName, middleName, post);
    }

    public void update(String lastName, String firstName, String middleName, String post) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = (middleName == null || middleName.isBlank()) ? null : middleName.trim();
        this.post = post;
    }

    public Long getId() { return id; }
    public short getPosition() { return position; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getPost() { return post; }
}