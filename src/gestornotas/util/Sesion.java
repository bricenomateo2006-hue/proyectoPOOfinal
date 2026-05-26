package gestornotas.util;

import gestornotas.modelo.Usuario;

/**
 * Singleton que guarda el usuario autenticado durante toda la sesión.
 * Se inicializa al hacer login y se limpia al cerrar sesión.
 */
public class Sesion {

    private static Sesion instancia;
    private Usuario usuarioActual;

    private Sesion() {}

    public static Sesion get() {
        if (instancia == null) instancia = new Sesion();
        return instancia;
    }

    public Usuario getUsuario()                  { return usuarioActual; }
    public void    setUsuario(Usuario u)          { this.usuarioActual = u; }

    public boolean esAdmin() {
        return usuarioActual != null && "ADMIN".equals(usuarioActual.getRolNombre());
    }

    public int getUsuarioId() {
        return usuarioActual != null ? usuarioActual.getId() : -1;
    }

    public void cerrar() {
        usuarioActual = null;
    }
}