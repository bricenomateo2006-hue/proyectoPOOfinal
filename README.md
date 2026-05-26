# Gestor de Trabajos — ProyectoFinalPOO

Aplicación de escritorio desarrollada en Java con interfaz gráfica Swing para la gestión de tickets de trabajo, eventos y categorías dentro de una institución. Proyecto final de la asignatura **Programación Orientada a Objetos** — UTS, Grupo E194.

**Autor:** Mathius Joel Briceño

---

## Descripción

El sistema permite registrar y hacer seguimiento a trabajos (tickets de servicio) asignados a usuarios, junto con eventos del calendario y las categorías que los clasifican. Cuenta con dos roles: **Administrador** y **Usuario**, con permisos diferenciados en cada sección.

---

## Requisitos previos

| Requisito | Versión recomendada |
|---|---|
| Java JDK | 17 o superior |
| PostgreSQL | 13 o superior |
| NetBeans IDE | 17 o superior |
| Driver JDBC PostgreSQL | postgresql-42.x.jar |

---

## Configuración de la base de datos

### 1. Crear la base de datos

```sql
CREATE DATABASE gestor_notas;
```

### 2. Crear las tablas

```sql
-- Roles
CREATE TABLE roles (
    id     SERIAL PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL UNIQUE
);
INSERT INTO roles (nombre) VALUES ('ADMIN'), ('USUARIO');

-- Usuarios
CREATE TABLE usuarios (
    id         SERIAL PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(64)  NOT NULL,  -- hash SHA-256
    rol_id     INTEGER      NOT NULL REFERENCES roles(id),
    activo     BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- Categorias (compartidas por trabajos y eventos, diferenciadas por tipo)
CREATE TABLE categorias (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(80)  NOT NULL,
    descripcion TEXT,
    tipo        VARCHAR(10)  NOT NULL CHECK (tipo IN ('TRABAJO', 'EVENTO')),
    creado_en   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- Trabajos (tickets de servicio)
CREATE TABLE trabajos (
    id                SERIAL PRIMARY KEY,
    solicitante       VARCHAR(100) NOT NULL,
    beneficiario      VARCHAR(100) NOT NULL,
    oficina           VARCHAR(100) NOT NULL,
    fecha_postulacion DATE         NOT NULL,
    descripcion       TEXT,
    estado            VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                          CHECK (estado IN ('PENDIENTE', 'SOLUCIONADO', 'RECHAZADO')),
    urgente           BOOLEAN      NOT NULL DEFAULT FALSE,
    categoria_id      INTEGER      REFERENCES categorias(id) ON DELETE SET NULL,
    asignado_id       INTEGER      NOT NULL REFERENCES usuarios(id),
    creado_por_id     INTEGER      NOT NULL REFERENCES usuarios(id),
    creado_en         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- Eventos del calendario
CREATE TABLE eventos (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha       DATE         NOT NULL,
    tipo        VARCHAR(10)  NOT NULL DEFAULT 'PERSONAL'
                    CHECK (tipo IN ('PERSONAL', 'GLOBAL')),
    categoria_id INTEGER     REFERENCES categorias(id) ON DELETE SET NULL,
    usuario_id  INTEGER      NOT NULL REFERENCES usuarios(id),
    creado_en   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para silenciar eventos por usuario
CREATE TABLE eventos_silenciados (
    evento_id  INTEGER NOT NULL REFERENCES eventos(id),
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
    PRIMARY KEY (evento_id, usuario_id)
);
```

### 3. Crear el usuario administrador inicial

La contraseña se almacena como hash SHA-256. Para la clave `admin123` el hash es:

```sql
INSERT INTO usuarios (nombre, email, password, rol_id)
VALUES (
    'Administrador',
    'admin@uts.edu.co',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    1
);
```

> Para generar el hash de otra contraseña puedes usar el método `Seguridad.hashSHA256(String)` incluido en el proyecto, o cualquier generador SHA-256 en línea.

---

## Configuración del proyecto

### Ajustar la conexión a la base de datos

Abre el archivo `src/gestornotas/db/Conexion.java` y edita las tres constantes según tu entorno:

```java
private static final String URL     = "jdbc:postgresql://localhost:5432/gestor_notas";
private static final String USUARIO = "postgres";
private static final String CLAVE   = "tu_contraseña";
```

### Agregar el driver JDBC en NetBeans

1. Clic derecho sobre el proyecto → **Properties**.
2. Ir a **Libraries** → **Add JAR/Folder**.
3. Seleccionar el archivo `postgresql-42.x.jar`.

---

## Estructura del proyecto

```
ProyectoFinalPOO/
└── src/
    └── gestornotas/
        ├── Main.java                  # Punto de entrada
        ├── db/
        │   ├── Conexion.java          # Singleton de conexión JDBC
        │   ├── UsuarioDAO.java        # CRUD + login de usuarios
        │   ├── TrabajoDAO.java        # CRUD de trabajos/tickets
        │   ├── EventoDAO.java         # CRUD + silenciar/activar eventos
        │   └── CategoriaDAO.java      # CRUD de categorías
        ├── modelo/
        │   ├── Usuario.java
        │   ├── Trabajo.java
        │   ├── Evento.java
        │   └── Categoria.java
        ├── util/
        │   ├── Sesion.java            # Singleton de sesión activa
        │   └── Seguridad.java         # Hash SHA-256 de contraseñas
        └── vista/
            ├── VentanaLogin.java      # Pantalla de inicio de sesión
            ├── VentanaRegistro.java   # Registro de nuevo usuario
            ├── MenuPrincipal.java     # Ventana principal con navegación
            ├── VistaTrabajos.java     # Gestión de trabajos/tickets
            ├── VistaEventos.java      # Gestión de eventos
            └── VistaCategorias.java   # Gestión de categorías
```

---

## Funcionalidades por rol

### Administrador

| Sección | Acciones disponibles |
|---|---|
| Trabajos | Crear, editar, eliminar, buscar, marcar como urgente, asignar a usuario |
| Eventos | Crear PERSONAL y GLOBAL, editar, eliminar, silenciar/activar |
| Categorías | Crear, editar y eliminar categorías para trabajos y eventos |
| Reportes | Ver conteo total de trabajos, categorías y eventos activos |

### Usuario

| Sección | Acciones disponibles |
|---|---|
| Trabajos | Ver los trabajos asignados a él, marcar como SOLUCIONADO o RECHAZADO |
| Eventos | Ver eventos propios y globales, crear eventos PERSONALES, silenciar/activar |
| Categorías | Solo lectura (visualización) |

---

## Estados de un trabajo

| Estado | Descripción |
|---|---|
| `PENDIENTE` | Recién creado, esperando atención del usuario asignado |
| `SOLUCIONADO` | El usuario asignado lo marcó como resuelto |
| `RECHAZADO` | El usuario asignado lo rechazó |

Los trabajos urgentes se muestran en la parte superior de la tabla con resaltado visual en color rojo.

---

## Seguridad

- Las contraseñas nunca se almacenan en texto plano; se aplica hash **SHA-256** antes de guardar o comparar.
- El rol del usuario se verifica en cada operación sensible (crear eventos GLOBALES, eliminar cualquier registro, etc.) a través del singleton `Sesion`.
- Un usuario inactivo no puede iniciar sesión aunque sus credenciales sean correctas.

---

## Cómo ejecutar

1. Clonar o descomprimir el proyecto.
2. Abrirlo en **NetBeans** como proyecto existente.
3. Agregar el driver PostgreSQL a las librerías del proyecto.
4. Ajustar `Conexion.java` con los datos de tu base de datos.
5. Ejecutar el script SQL de creación de tablas.
6. Insertar el usuario administrador inicial.
7. Ejecutar el proyecto con **Run Project (F6)**.

---

## Notas adicionales

- El proyecto fue desarrollado y probado con **NetBeans 17** y **JDK 21**.
- La interfaz usa el Look & Feel del sistema operativo (`UIManager.getSystemLookAndFeel()`), por lo que la apariencia puede variar ligeramente entre Windows, Linux y macOS.
- Si al abrir el proyecto aparecen errores de compilación relacionados con `text blocks` (las cadenas `"""`), asegúrate de compilar con Java 15 o superior.
