package com.api.portaldatransparencia.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseInitializerService {

    @Autowired
    private DataSource dataSource;

    @Transactional
    public void initializeDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS api");
            stmt.execute("USE api");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar o banco de dados", e);
        }
    }
}
