package com.calumma.backend.model.entity;


import javax.persistence.*;
import java.util.Calendar;
import java.util.UUID;

@MappedSuperclass
public abstract class CalummaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String externalId;

    private Calendar insertDate;
    private Calendar deletionDate;

    private boolean isDeleted;

    @PrePersist
    public void prePersist() {
        if(externalId==null) {
            externalId = UUID.randomUUID().toString();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Calendar getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Calendar insertDate) {
        this.insertDate = insertDate;
    }

    public Calendar getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Calendar deletionDate) {
        this.deletionDate = deletionDate;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean getDeleted() {
        return isDeleted;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public boolean isDeleted() {
        return isDeleted;
    }


}
