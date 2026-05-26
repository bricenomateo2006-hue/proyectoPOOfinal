package gestornotas.db;

import gestornotas.modelo.Usuario;
import gestornotas.util.Seguridad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    /** Autentica usuario. Retorna el objeto Usuario o null si falla. */
    public Usuario login(String email, String password) {
        String hash = Seguridad.hashSHA256(password);
        String sql  = """
            SELECT u.id, u.nombre, u.email, u.password,
                   u.rol_id, r.nombre AS rol_nombre, u.activo, u.creado_en
            FROM usuarios u
            JOIN roles r ON u.rol_id = r.id
            WHERE u.email = ? AND u.password = ? AND u.activo = TRUE
            """;
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error en login: " + e.getMessage());
        }
        return null;
    }

    /** Registra un nuevo usuario con rol USUARIO (id=2) por defecto. */
    public boolean registrar(String nombre, String email, String password) {
        String hash = Seguridad.hashSHA256(password);
        String sql  = "INSERT INTO usuarios (nombre, email, password, rol_id) VALUES (?, ?, ?, 2)";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setString(3, hash);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar: " + e.getMessage());
            return false;
        }
    }

    /** Lista todos los usuarios (solo para ADMIN). */
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = """
            SELECT u.id, u.nombre, u.email, u.password,
                   u.rol_id, r.nombre AS rol_nombre, u.activo, u.creado_en
            FROM usuarios u
            JOIN roles r ON u.rol_id = r.id
            ORDER BY u.id
            """;
        try (Statement st = Conexion.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    /** Activa o desactiva un usuario (solo ADMIN). */
    public boolean cambiarEstado(int id, boolean activo) {
        String sql = "UPDATE usuarios SET activo = ? WHERE id = ?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
            return false;
        }
    }

    /** Cambia el rol de un usuario (solo ADMIN). */
    public boolean cambiarRol(int id, int rolId) {
        String sql = "UPDATE usuarios SET rol_id = ? WHERE id = ?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, rolId);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar rol: " + e.getMessage());
            return false;
        }
    }

    /** Verifica si un email ya existe en la base de datos. */
    public boolean emailExiste(String email) {
        String sql = "SELECT 1 FROM usuarios WHERE email = ?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getInt("rol_id"),
            rs.getString("rol_nombre"),
            rs.getBoolean("activo"),
            rs.getTimestamp("creado_en")
        );
    }
}