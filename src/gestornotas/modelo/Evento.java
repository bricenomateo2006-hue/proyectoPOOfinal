package gestornotas.modelo;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Representa un evento del sistema.
 * Puede ser GLOBAL (visible para todos) o PERSONAL (solo el creador).
 */
public class Evento {

    private int       id;
    private String    nombre;
    private String    descripcion;
    private Date      fecha;
    private String    tipo;           // GLOBAL | PERSONAL
    private int       categoriaId;
    private String    categoriaNombre;
    private int       usuarioId;
    private String    usuarioNombre;
    private Timestamp creadoEn;
    private boolean   silenciado;

    public Evento() {}

    public Evento(int id, String nombre, String descripcion, Date fecha,
                  String tipo, int categoriaId, String categoriaNombre,
                  int usuarioId, String usuarioNombre,
                  Timestamp creadoEn, boolean silenciado) {
        this.id              = id;
        this.nombre          = nombre;
        this.descripcion     = descripcion;
        this.fecha           = fecha;
        this.tipo            = tipo;
        this.categoriaId     = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.usuarioId       = usuarioId;
        this.usuarioNombre   = usuarioNombre;
        this.creadoEn        = creadoEn;
        this.silenciado      = silenciado;
    }

    public int       getId()                              { return id; }
    public void      setId(int id)                        { this.id = id; }
    public String    getNombre()                          { return nombre; }
    public void      setNombre(String nombre)             { this.nombre = nombre; }
    public String    getDescripcion()                     { return descripcion; }
    public void      setDescripcion(String d)             { this.descripcion = d; }
    public Date      getFecha()                           { return fecha; }
    public void      setFecha(Date fecha)                 { this.fecha = fecha; }
    public String    getTipo()                            { return tipo; }
    public void      setTipo(String tipo)                 { this.tipo = tipo; }
    public int       getCategoriaId()                     { return categoriaId; }
    public void      setCategoriaId(int id)               { this.categoriaId = id; }
    public String    getCategoriaNombre()                 { return categoriaNombre; }
    public void      setCategoriaNombre(String n)         { this.categoriaNombre = n; }
    public int       getUsuarioId()                       { return usuarioId; }
    public void      setUsuarioId(int id)                 { this.usuarioId = id; }
    public String    getUsuarioNombre()                   { return usuarioNombre; }
    public void      setUsuarioNombre(String n)           { this.usuarioNombre = n; }
    public Timestamp getCreadoEn()                        { return creadoEn; }
    public void      setCreadoEn(Timestamp t)             { this.creadoEn = t; }
    public boolean   isSilenciado()                       { return silenciado; }
    public void      setSilenciado(boolean s)             { this.silenciado = s; }

    public boolean esMio(int usuarioActualId) { return this.usuarioId == usuarioActualId; }

    @Override
    public String toString() { return nombre; }
}
