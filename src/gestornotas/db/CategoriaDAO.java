package gestornotas.db;

import gestornotas.modelo.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla 'categorias'.
 * Las categorías están tipificadas: TRABAJO o EVENTO.
 */
public class CategoriaDAO {

    public boolean insertar(Categoria c) {
        String sql = "INSERT INTO categorias (nombre, descripcion, tipo) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setString(3, c.getTipo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar categoría: " + e.getMessage());
            return false;
        }
    }

    /** Lista todas las categorías sin filtro. */
    public List<Categoria> listar() {
        return listarPorTipo(null);
    }

    /** Lista solo las categorías del tipo indicado ('TRABAJO' o 'EVENTO'). */
    public List<Categoria> listarPorTipo(String tipo) {
        String sql = tipo == null
            ? "SELECT id, nombre, descripcion, tipo, creado_en FROM categorias ORDER BY tipo, nombre"
            : "SELECT id, nombre, descripcion, tipo, creado_en FROM categorias WHERE tipo = ? ORDER BY nombre";
        List<Categoria> lista = new ArrayList<>();
        try {
            if (tipo == null) {
                try (Statement st = Conexion.getConexion().createStatement();
                     ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            } else {
                try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
                    ps.setString(1, tipo);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) lista.add(mapear(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar categorías: " + e.getMessage());
        }
        return lista;
    }

    public Categoria buscarPorId(int id) {
        String sql = "SELECT id, nombre, descripcion, tipo, creado_en FROM categorias WHERE id = ?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar categoría: " + e.getMessage());
        }
        return null;
    }

    public boolean actualizar(Categoria c) {
        String sql = "UPDATE categorias SET nombre=?, descripcion=?, tipo=? WHERE id=?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setString(3, c.getTipo());
            ps.setInt(4, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            return false;
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getString("tipo"),
            rs.getTimestamp("creado_en")
        );
    }
}
