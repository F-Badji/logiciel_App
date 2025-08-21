package com.logicielapp.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Gestionnaire de base de données avec pool de connexions HikariCP
 * Singleton pattern pour gérer les connexions MySQL
 */
public class DatabaseManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;
    private HikariDataSource dataSource;
    
    // Configuration par défaut
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/logiciel_App";
    private static final String DEFAULT_DB_USERNAME = "root";
    private static final String DEFAULT_DB_PASSWORD = "";
    
    private DatabaseManager() {
        // Constructeur privé pour Singleton
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Initialise la base de données et le pool de connexions
     */
    public void initializeDatabase() throws SQLException {
        try {
            Properties props = loadDatabaseProperties();
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url", DEFAULT_DB_URL));
            config.setUsername(props.getProperty("db.username", DEFAULT_DB_USERNAME));
            config.setPassword(props.getProperty("db.password", DEFAULT_DB_PASSWORD));
            
            // Configuration du pool
            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setLeakDetectionThreshold(60000);
            
            // Optimisations MySQL
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(config);
            
            // Test de connexion
            try (Connection conn = getConnection()) {
                logger.info("Connexion à la base de données établie avec succès");
                logger.info("URL: {}", props.getProperty("db.url", DEFAULT_DB_URL));
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation de la base de données", e);
            throw new SQLException("Impossible de se connecter à la base de données", e);
        }
    }
    
    /**
     * Charge les propriétés de configuration de la base de données
     */
    private Properties loadDatabaseProperties() {
        Properties props = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input != null) {
                props.load(input);
                logger.info("Configuration de base de données chargée depuis database.properties");
            } else {
                logger.warn("Fichier database.properties introuvable, utilisation des valeurs par défaut");
            }
        } catch (IOException e) {
            logger.warn("Erreur lors du chargement de database.properties, utilisation des valeurs par défaut", e);
        }
        
        return props;
    }
    
    /**
     * Obtient une connexion depuis le pool
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("La base de données n'est pas initialisée");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Vérifie si la base de données est disponible
     */
    public boolean isDatabaseAvailable() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            logger.error("La base de données n'est pas disponible", e);
            return false;
        }
    }
    
    /**
     * Ferme le pool de connexions
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Pool de connexions fermé");
        }
    }
    
    /**
     * Retourne les statistiques du pool de connexions
     */
    public String getPoolStats() {
        if (dataSource != null) {
            return String.format(
                "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "Pool non initialisé";
    }
}
