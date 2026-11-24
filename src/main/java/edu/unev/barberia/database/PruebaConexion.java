package edu.unev.barberia.database;

/** Ejecutable de demostración: no modifica tablas ni datos. */
public final class PruebaConexion {
    private PruebaConexion() {}
    public static void main(String[] args) throws Exception {
        try (var conexion = ConexionBD.getConexion(); var consulta = conexion.createStatement()) {
            if (!conexion.isValid(5)) throw new IllegalStateException("La conexión no es válida.");
            try (var resultado = consulta.executeQuery("SELECT DATABASE(), VERSION()")) {
                resultado.next();
                System.out.println("CONEXIÓN JDBC EXITOSA");
                System.out.println("Base de datos: " + resultado.getString(1));
                System.out.println("Servidor: " + resultado.getString(2));
            }
            try (var resultado = consulta.executeQuery("SHOW TABLES")) {
                int cantidad=0;
                while (resultado.next()) { System.out.println("Tabla: " + resultado.getString(1)); cantidad++; }
                if (cantidad!=11) throw new IllegalStateException("Se esperaban 11 tablas; se encontraron " + cantidad);
                System.out.println("Tablas verificadas: " + cantidad);
            }
        }
    }
}
