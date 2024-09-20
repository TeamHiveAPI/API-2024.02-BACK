package com.api.portaldatransparencia.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class DatabaseInitializer {
    @Id
    private Long id;
    private boolean initialized;
}
