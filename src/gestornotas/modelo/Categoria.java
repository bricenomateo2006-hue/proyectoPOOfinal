package gestornotas.modelo;

import java.sql.Timestamp;

/**
 * Representa una categoría del sistema.
 * tipo = 'TRABAJO' → categorías para trabajos/tickets
 * tipo = 'EVENTO'  → categorías para eventos del calendario
 */
public class Categoria {

    private int       id;
    private String    nombre;
    private String    descripcion;
    private String    tipo;        // 'TRABAJO' | 'EVENTO'
    private Timestamp creadoEn;

    public Categoria() {}

    public Categoria(int id, String nombre, String descripcion,
                     String tipo, Timestamp creadoEn) {
        this.id          = id;
        this.nombre      = nombre;
        this.descripcion = descripcion;
        this.tipo        = tipo;
        this.creadoEn    = creadoEn;
    }

    public int       getId()                        { return id; }
    public void      setId(int id)                  { this.id = id; }
    public String    getNombre()                    { return nombre; }
    public void      setNombre(String n)            { this.nombre = n; }
    public String    getDescripcion()               { return descripcion; }
    public void      setDescripcion(String d)       { this.descripcion = d; }
    public String    getTipo()                      { return tipo; }
    public void      setTipo(String t)              { this.tipo = t; }
    public Timestamp getCreadoEn()                  { return creadoEn; }
    public void      setCreadoEn(Timestamp t)       { this.creadoEn = t; }

    @Override
    public String toString() { return nombre; }
}
