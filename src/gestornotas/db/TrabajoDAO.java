package gestornotas.db;

import gestornotas.modelo.Trabajo;
import gestornotas.util.Sesion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla 'trabajos'.
 * ADMIN : ve todos; puede crear, editar, eliminar.
 * USUARIO: solo ve los asignados a él; puede cambiar estado.
 */
public class TrabajoDAO {

    public boolean insertar(Trabajo t) {
        String sql = """
            INSERT INTO trabajos
                (solicitante, beneficiario, oficina, fecha_postulacion,
                 descripcion, estado, urgente, categoria_id, asignado_id, creado_por_id)
            VALUES (?, ?, ?, ?, ?, 'PENDIENTE', ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, t.getSolicitante());
            ps.setString(2, t.getBeneficiario());
            ps.setString(3, t.getOficina());
            ps.setDate(4, t.getFechaPostulacion());
            ps.setString(5, t.getDescripcion());
            ps.setBoolean(6, t.isUrgente());
            if (t.getCategoriaId() > 0) ps.setInt(7, t.getCategoriaId());
            else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, t.getAsignadoId());
            ps.setInt(9, Sesion.get().getUsuarioId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar trabajo: " + e.getMessage());
            return false;
        }
    }

    public List<Trabajo> listar() {
        String condicion = Sesion.get().esAdmin()
                ? ""
                : " AND t.asignado_id = " + Sesion.get().getUsuarioId();
        // Urgentes primero, luego por id descendente
        String sql = sqlBase() + condicion + " ORDER BY t.urgente DESC, t.id DESC";
        List<Trabajo> lista = new ArrayList<>();
        try (Statement st = Conexion.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("Error al listar trabajos: " + e.getMessage());
        }
        return lista;
    }

    public List<Trabajo> buscar(String termino) {
        String condUsuario = Sesion.get().esAdmin() ? "" : " AND t.asignado_id = ?";
        String sql = sqlBase()
                + " AND (LOWER(t.solicitante) LIKE LOWER(?)"
                + "   OR LOWER(t.beneficiario) LIKE LOWER(?))"
                + condUsuario
                + " ORDER BY t.urgente DESC, t.id DESC";
        List<Trabajo> lista = new ArrayList<>();
        String like = "%" + termino + "%";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            if (!Sesion.get().esAdmin()) ps.setInt(3, Sesion.get().getUsuarioId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar trabajos: " + e.getMessage());
        }
        return lista;
    }

    public Trabajo buscarPorId(int id) {
        String sql = sqlBase() + " AND t.id = ?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar trabajo: " + e.getMessage());
        }
        return null;
    }

    public boolean actualizar(Trabajo t) {
        String sql = """
            UPDATE trabajos
            SET solicitante=?, beneficiario=?, oficina=?,
                fecha_postulacion=?, descripcion=?, urgente=?,
                categoria_id=?, asignado_id=?
            WHERE id=?
            """;
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, t.getSolicitante());
            ps.setString(2, t.getBeneficiario());
            ps.setString(3, t.getOficina());
            ps.setDate(4, t.getFechaPostulacion());
            ps.setString(5, t.getDescripcion());
            ps.setBoolean(6, t.isUrgente());
            if (t.getCategoriaId() > 0) ps.setInt(7, t.getCategoriaId());
            else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, t.getAsignadoId());
            ps.setInt(9, t.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar trabajo: " + e.getMessage());
            return false;
        }
    }

    public boolean cambiarEstado(int trabajoId, String nuevoEstado) {
        String sql = """
            UPDATE trabajos
            SET estado = ?
            WHERE id = ? AND asignado_id = ? AND estado = 'PENDIENTE'
            """;
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, trabajoId);
            ps.setInt(3, Sesion.get().getUsuarioId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM trabajos WHERE id = ?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar trabajo: " + e.getMessage());
            return false;
        }
    }

    private String sqlBase() {
        return """
            SELECT t.id, t.solicitante, t.beneficiario, t.oficina,
                   t.fecha_postulacion, t.descripcion, t.estado, t.urgente,
                   t.categoria_id,
                   COALESCE(c.nombre, '—') AS categoria_nombre,
                   t.asignado_id,
                   ua.nombre  AS asignado_nombre,
                   t.creado_por_id,
                   uc.nombre  AS creado_por_nombre,
                   t.creado_en
            FROM trabajos t
            JOIN usuarios ua  ON t.asignado_id   = ua.id
            JOIN usuarios uc  ON t.creado_por_id = uc.id
            LEFT JOIN categorias c ON t.categoria_id = c.id
            WHERE 1=1
            """;
    }

    private Trabajo mapear(ResultSet rs) throws SQLException {
        return new Trabajo(
            rs.getInt("id"),
            rs.getString("solicitante"),
            rs.getString("beneficiario"),
            rs.getString("oficina"),
            rs.getDate("fecha_postulacion"),
            rs.getString("descripcion"),
            rs.getString("estado"),
            rs.getBoolean("urgente"),
            rs.getInt("categoria_id"),
            rs.getString("categoria_nombre"),
            rs.getInt("asignado_id"),
            rs.getString("asignado_nombre"),
            rs.getInt("creado_por_id"),
            rs.getString("creado_por_nombre"),
            rs.getTimestamp("creado_en")
        );
    }
}
