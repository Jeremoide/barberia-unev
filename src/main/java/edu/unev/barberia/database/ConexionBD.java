package edu.unev.barberia.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Utilidad JDBC. Cada llamada entrega una conexión que el llamador debe cerrar. */
public final class ConexionBD {
    private ConexionBD() {}

    public static Connection getConexion() throws SQLException {
        Properties config = new Properties();
        Path archivo = Path.of(System.getProperty("barberia.db.config", "config/database.properties"));
        if (Files.exists(archivo)) {
            try (var entrada = Files.newInputStream(archivo)) {
                config.load(entrada);
            } catch (IOException ex) {
                throw new SQLException("No se pudo leer la configuración de la base de datos.", ex);
            }
        }
        String url = valor("BARBERIA_DB_URL", config, "url", "jdbc:mariadb://127.0.0.1:3306/barberia_unev");
        String usuario = valor("BARBERIA_DB_USER", config, "user", "");
        String clave = valor("BARBERIA_DB_PASSWORD", config, "password", "");
        if (usuario.isBlank() || clave.isBlank()) {
            throw new SQLException("Configure BARBERIA_DB_USER/PASSWORD o config/database.properties.");
        }
        if (!url.startsWith("jdbc:mariadb:")) {
            throw new SQLException("Utilice una URL jdbc:mariadb: para el conector configurado.");
        }
        return DriverManager.getConnection(url, usuario, clave);
    }

    private static String valor(String variable, Properties config, String propiedad, String defecto) {
        String valor = System.getenv(variable);
        return valor == null ? config.getProperty(propiedad, defecto) : valor;
    }
}
