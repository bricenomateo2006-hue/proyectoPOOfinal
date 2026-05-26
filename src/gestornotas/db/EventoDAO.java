package gestornotas.db;

import gestornotas.modelo.Evento;
import gestornotas.util.Sesion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventoDAO {

    public boolean insertar(Evento e) {
        if (!Sesion.get().esAdmin() && "GLOBAL".equals(e.getTipo())) {
            System.err.println("Permiso denegado: solo el administrador puede crear eventos GLOBALES.");
            return false;
        }
        String sql = "INSERT INTO eventos (nombre, descripcion, fecha, tipo, categoria_id, usuario_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getDescripcion());
            ps.setDate(3, e.getFecha());
            ps.setString(4, e.getTipo());
            if (e.getCategoriaId() > 0) ps.setInt(5, e.getCategoriaId());
            else ps.setNull(5, Types.INTEGER);
            ps.setInt(6, Sesion.get().getUsuarioId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Error al insertar evento: " + ex.getMessage());
            return false;
        }
    }

    public List<Evento> listar() {
        int uid = Sesion.get().getUsuarioId();
        String sql = """
            SELECT e.id, e.nombre, e.descripcion, e.fecha, e.tipo,
                   e.categoria_id, COALESCE(c.nombre,'—') AS categoria_nombre,
                   e.usuario_id, u.nombre AS usuario_nombre, e.creado_en,
                   EXISTS (
                       SELECT 1 FROM eventos_silenciados es
                       WHERE es.evento_id = e.id AND es.usuario_id = ?
                   ) AS silenciado
            FROM eventos e
            JOIN usuarios u ON e.usuario_id = u.id
            LEFT JOIN categorias c ON e.categoria_id = c.id
            WHERE (e.tipo = 'GLOBAL' OR e.usuario_id = ?)
              AND NOT EXISTS (
                  SELECT 1 FROM eventos_silenciados es
                  WHERE es.evento_id = e.id AND es.usuario_id = ?
              )
            ORDER BY e.fecha ASC, e.id ASC
            """;
        List<Evento> lista = new ArrayList<>();
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, uid); ps.setInt(2, uid); ps.setInt(3, uid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            System.err.println("Error al listar eventos: " + ex.getMessage());
        }
        return lista;
    }

    public List<Evento> listarTodos() {
        int uid = Sesion.get().getUsuarioId();
        String sql = """
            SELECT e.id, e.nombre, e.descripcion, e.fecha, e.tipo,
                   e.categoria_id, COALESCE(c.nombre,'—') AS categoria_nombre,
                   e.usuario_id, u.nombre AS usuario_nombre, e.creado_en,
                   EXISTS (
                       SELECT 1 FROM eventos_silenciados es
                       WHERE es.evento_id = e.id AND es.usuario_id = ?
                   ) AS silenciado
            FROM eventos e
            JOIN usuarios u ON e.usuario_id = u.id
            LEFT JOIN categorias c ON e.categoria_id = c.id
            WHERE (e.tipo = 'GLOBAL' OR e.usuario_id = ?)
            ORDER BY e.fecha ASC, e.id ASC
            """;
        List<Evento> lista = new ArrayList<>();
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, uid); ps.setInt(2, uid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            System.err.println("Error al listar todos los eventos: " + ex.getMessage());
        }
        return lista;
    }

    public Evento buscarPorId(int id) {
        int uid = Sesion.get().getUsuarioId();
        String sql = """
            SELECT e.id, e.nombre, e.descripcion, e.fecha, e.tipo,
                   e.categoria_id, COALESCE(c.nombre,'—') AS categoria_nombre,
                   e.usuario_id, u.nombre AS usuario_nombre, e.creado_en,
                   EXISTS (
                       SELECT 1 FROM eventos_silenciados es
                       WHERE es.evento_id = e.id AND es.usuario_id = ?
                   ) AS silenciado
            FROM eventos e
            JOIN usuarios u ON e.usuario_id = u.id
            LEFT JOIN categorias c ON e.categoria_id = c.id
            WHERE e.id = ?
            """;
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, uid); ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException ex) {
            System.err.println("Error al buscar evento: " + ex.getMessage());
        }
        return null;
    }

    public boolean actualizar(Evento e) {
        try {
            PreparedStatement ps;
            if (Sesion.get().esAdmin()) {
                String sql = "UPDATE eventos SET nombre=?, descripcion=?, fecha=?, tipo=?, categoria_id=? WHERE id=?";
                ps = Conexion.getConexion().prepareStatement(sql);
                ps.setString(1, e.getNombre());
                ps.setString(2, e.getDescripcion());
                ps.setDate(3, e.getFecha());
                ps.setString(4, e.getTipo());
                if (e.getCategoriaId() > 0) ps.setInt(5, e.getCategoriaId());
                else ps.setNull(5, Types.INTEGER);
                ps.setInt(6, e.getId());
            } else {
                String sql = "UPDATE eventos SET nombre=?, descripcion=?, fecha=?, tipo='PERSONAL', categoria_id=? WHERE id=? AND usuario_id=?";
                ps = Conexion.getConexion().prepareStatement(sql);
                ps.setString(1, e.getNombre());
                ps.setString(2, e.getDescripcion());
                ps.setDate(3, e.getFecha());
                if (e.getCategoriaId() > 0) ps.setInt(4, e.getCategoriaId());
                else ps.setNull(4, Types.INTEGER);
                ps.setInt(5, e.getId());
                ps.setInt(6, Sesion.get().getUsuarioId());
            }
            boolean ok = ps.executeUpdate() > 0;
            ps.close();
            return ok;
        } catch (SQLException ex) {
            System.err.println("Error al actualizar evento: " + ex.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(
                "DELETE FROM eventos_silenciados WHERE evento_id = ?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error al limpiar silenciados: " + ex.getMessage());
        }
        try {
            PreparedStatement ps;
            if (Sesion.get().esAdmin()) {
                ps = Conexion.getConexion().prepareStatement("DELETE FROM eventos WHERE id = ?");
                ps.setInt(1, id);
            } else {
                ps = Conexion.getConexion().prepareStatement("DELETE FROM eventos WHERE id = ? AND usuario_id = ?");
                ps.setInt(1, id); ps.setInt(2, Sesion.get().getUsuarioId());
            }
            boolean ok = ps.executeUpdate() > 0;
            ps.close();
            return ok;
        } catch (SQLException ex) {
            System.err.println("Error al eliminar evento: " + ex.getMessage());
            return false;
        }
    }

    public boolean silenciar(int eventoId) {
        String sql = "INSERT INTO eventos_silenciados (evento_id, usuario_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, eventoId); ps.setInt(2, Sesion.get().getUsuarioId());
            return ps.executeUpdate() >= 0;
        } catch (SQLException ex) {
            System.err.println("Error al silenciar: " + ex.getMessage());
            return false;
        }
    }

    public boolean desilenciar(int eventoId) {
        String sql = "DELETE FROM eventos_silenciados WHERE evento_id = ? AND usuario_id = ?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, eventoId); ps.setInt(2, Sesion.get().getUsuarioId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Error al desilenciar: " + ex.getMessage());
            return false;
        }
    }

    private Evento mapear(ResultSet rs) throws SQLException {
        return new Evento(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getDate("fecha"),
            rs.getString("tipo"),
            rs.getInt("categoria_id"),
            rs.getString("categoria_nombre"),
            rs.getInt("usuario_id"),
            rs.getString("usuario_nombre"),
            rs.getTimestamp("creado_en"),
            rs.getBoolean("silenciado")
        );
    }
}
