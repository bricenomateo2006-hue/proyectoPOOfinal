package gestornotas.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL     = "jdbc:postgresql://localhost:5432/gestor_notas";
    private static final String USUARIO = "postgres";
    private static final String CLAVE   = "1234"; // ← cambia por tu contraseña

    private static Connection instancia = null;

    private Conexion() {}

    public static Connection getConexion() {
        try {
            if (instancia == null || instancia.isClosed()) {
                Class.forName("org.postgresql.Driver");
                instancia = DriverManager.getConnection(URL, USUARIO, CLAVE);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver no encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error de conexión SQL: " + e.getMessage());
        }
        return instancia;
    }

    public static void cerrar() {
        try {
            if (instancia != null && !instancia.isClosed()) instancia.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar: " + e.getMessage());
        }
    }
}