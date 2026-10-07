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

@Entity
@Table(name = "author")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    /** Порядковый номер автора в заявке: 1 или 2. */
    @Column(nullable = false)
    private short position;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Funding funding;

    protected Author() {
    }

    Author(Application application, int position, String fullName, String groupName,
           String phone, String email, Funding funding) {
        this.application = application;
        this.position = (short) position;
        update(fullName, groupName, phone, email, funding);
    }

    public void update(String fullName, String groupName, String phone, String email, Funding funding) {
        this.fullName = fullName;
        this.groupName = groupName;
        this.phone = phone;
        this.email = email;
        this.funding = funding;
    }

    public Long getId() { return id; }
    public short getPosition() { return position; }
    public String getFullName() { return fullName; }
    public String getGroupName() { return groupName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public Funding getFunding() { return funding; }
}
