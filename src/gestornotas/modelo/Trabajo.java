package gestornotas.modelo;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Representa un trabajo/ticket en el sistema.
 * Un Admin lo crea y asigna a un usuario común.
 * El usuario asignado puede marcarlo como SOLUCIONADO o RECHAZADO.
 *
 * Estados posibles: PENDIENTE | SOLUCIONADO | RECHAZADO
 */
public class Trabajo {

    private int       id;
    private String    solicitante;
    private String    beneficiario;
    private String    oficina;
    private Date      fechaPostulacion;
    private String    descripcion;
    private String    estado;           // PENDIENTE | SOLUCIONADO | RECHAZADO
    private boolean   urgente;          // true = marcado como URGENTE
    private int       categoriaId;
    private String    categoriaNombre;
    private int       asignadoId;
    private String    asignadoNombre;
    private int       creadoPorId;
    private String    creadoPorNombre;
    private Timestamp creadoEn;

    public Trabajo() {}

    public Trabajo(int id, String solicitante, String beneficiario, String oficina,
                   Date fechaPostulacion, String descripcion, String estado,
                   boolean urgente, int categoriaId, String categoriaNombre,
                   int asignadoId, String asignadoNombre,
                   int creadoPorId, String creadoPorNombre,
                   Timestamp creadoEn) {
        this.id               = id;
        this.solicitante      = solicitante;
        this.beneficiario     = beneficiario;
        this.oficina          = oficina;
        this.fechaPostulacion = fechaPostulacion;
        this.descripcion      = descripcion;
        this.estado           = estado;
        this.urgente          = urgente;
        this.categoriaId      = categoriaId;
        this.categoriaNombre  = categoriaNombre;
        this.asignadoId       = asignadoId;
        this.asignadoNombre   = asignadoNombre;
        this.creadoPorId      = creadoPorId;
        this.creadoPorNombre  = creadoPorNombre;
        this.creadoEn         = creadoEn;
    }

    public int       getId()                              { return id; }
    public void      setId(int id)                        { this.id = id; }
    public String    getSolicitante()                     { return solicitante; }
    public void      setSolicitante(String s)             { this.solicitante = s; }
    public String    getBeneficiario()                    { return beneficiario; }
    public void      setBeneficiario(String b)            { this.beneficiario = b; }
    public String    getOficina()                         { return oficina; }
    public void      setOficina(String o)                 { this.oficina = o; }
    public Date      getFechaPostulacion()                { return fechaPostulacion; }
    public void      setFechaPostulacion(Date f)          { this.fechaPostulacion = f; }
    public String    getDescripcion()                     { return descripcion; }
    public void      setDescripcion(String d)             { this.descripcion = d; }
    public String    getEstado()                          { return estado; }
    public void      setEstado(String estado)             { this.estado = estado; }
    public boolean   isUrgente()                          { return urgente; }
    public void      setUrgente(boolean urgente)          { this.urgente = urgente; }
    public int       getCategoriaId()                     { return categoriaId; }
    public void      setCategoriaId(int id)               { this.categoriaId = id; }
    public String    getCategoriaNombre()                 { return categoriaNombre; }
    public void      setCategoriaNombre(String n)         { this.categoriaNombre = n; }
    public int       getAsignadoId()                      { return asignadoId; }
    public void      setAsignadoId(int id)                { this.asignadoId = id; }
    public String    getAsignadoNombre()                  { return asignadoNombre; }
    public void      setAsignadoNombre(String n)          { this.asignadoNombre = n; }
    public int       getCreadoPorId()                     { return creadoPorId; }
    public void      setCreadoPorId(int id)               { this.creadoPorId = id; }
    public String    getCreadoPorNombre()                 { return creadoPorNombre; }
    public void      setCreadoPorNombre(String n)         { this.creadoPorNombre = n; }
    public Timestamp getCreadoEn()                        { return creadoEn; }
    public void      setCreadoEn(Timestamp t)             { this.creadoEn = t; }

    public boolean isPendiente() { return "PENDIENTE".equals(estado); }

    @Override
    public String toString() { return "[" + id + "] " + solicitante + " → " + beneficiario; }
}
