package gestornotas.modelo;

import java.sql.Timestamp;

public class Usuario {

    private int       id;
    private String    nombre;
    private String    email;
    private String    password;
    private int       rolId;
    private String    rolNombre;
    private boolean   activo;
    private Timestamp creadoEn;

    public Usuario() {}

    public Usuario(int id, String nombre, String email, String password,
                   int rolId, String rolNombre, boolean activo, Timestamp creadoEn) {
        this.id        = id;
        this.nombre    = nombre;
        this.email     = email;
        this.password  = password;
        this.rolId     = rolId;
        this.rolNombre = rolNombre;
        this.activo    = activo;
        this.creadoEn  = creadoEn;
    }

    public int       getId()                        { return id; }
    public void      setId(int id)                  { this.id = id; }
    public String    getNombre()                    { return nombre; }
    public void      setNombre(String nombre)       { this.nombre = nombre; }
    public String    getEmail()                     { return email; }
    public void      setEmail(String email)         { this.email = email; }
    public String    getPassword()                  { return password; }
    public void      setPassword(String password)   { this.password = password; }
    public int       getRolId()                     { return rolId; }
    public void      setRolId(int rolId)            { this.rolId = rolId; }
    public String    getRolNombre()                 { return rolNombre; }
    public void      setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }
    public boolean   isActivo()                     { return activo; }
    public void      setActivo(boolean activo)      { this.activo = activo; }
    public Timestamp getCreadoEn()                  { return creadoEn; }
    public void      setCreadoEn(Timestamp t)       { this.creadoEn = t; }

    @Override
    public String toString() { return nombre + " (" + rolNombre + ")"; }
}