package gestornotas.vista;

import gestornotas.db.Conexion;
import gestornotas.util.Sesion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Ventana principal del Gestor de Trabajos.
 * Navega entre secciones usando botones simples en el lado izquierdo.
 */
public class MenuPrincipal extends JFrame {

    private final JPanel     panelContenido;
    private final CardLayout cardLayout;

    private final VistaTrabajos   vistaTrabajos;
    private final VistaCategorias vistaCategorias;
    private final VistaEventos    vistaEventos;

    public MenuPrincipal() {
        setTitle("Gestor de Trabajos - UTS POO E194");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(750, 450));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSalida();
            }
        });

        // Sidebar de navegacion
        JPanel sidebar = crearSidebar();

        // Contenido central con CardLayout
        cardLayout     = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(Color.WHITE);

        vistaTrabajos   = new VistaTrabajos();
        vistaCategorias = new VistaCategorias();
        vistaEventos    = new VistaEventos();

        panelContenido.add(crearPanelBienvenida(), "INICIO");
        panelContenido.add(vistaTrabajos,          "TRABAJOS");
        panelContenido.add(vistaCategorias,        "CATEGORIAS");
        panelContenido.add(vistaEventos,           "EVENTOS");
        panelContenido.add(crearPanelReportes(),   "REPORTES");

        cardLayout.show(panelContenido, "INICIO");

        setLayout(new BorderLayout());
        add(sidebar,        BorderLayout.WEST);
        add(panelContenido, BorderLayout.CENTER);
    }

    // ----- Sidebar ----------------------------------------------------------

    private JPanel crearSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        sidebar.setPreferredSize(new Dimension(160, 0));

        // Nombre del usuario
        String nombreUsuario = Sesion.get().getUsuario().getNombre();
        String rol           = Sesion.get().esAdmin() ? "Administrador" : "Usuario";

        JLabel lblNombre = new JLabel("<html><center>" + nombreUsuario + "</center></html>", SwingConstants.CENTER);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNombre.setBorder(BorderFactory.createEmptyBorder(18, 8, 2, 8));

        JLabel lblRol = new JLabel(rol, SwingConstants.CENTER);
        lblRol.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblRol.setForeground(Color.GRAY);
        lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblRol.setBorder(BorderFactory.createEmptyBorder(0, 8, 14, 8));

        sidebar.add(lblNombre);
        sidebar.add(lblRol);

        // Separador
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(10));

        // Botones de navegacion
        sidebar.add(botonNav("Inicio",      "INICIO"));
        sidebar.add(botonNav("Trabajos",    "TRABAJOS"));
        sidebar.add(botonNav("Categorias",  "CATEGORIAS"));
        sidebar.add(botonNav("Eventos",     "EVENTOS"));
        sidebar.add(botonNav("Reportes",    "REPORTES"));

        sidebar.add(Box.createVerticalGlue());

        // Separador antes de salir
        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep2);

        // Boton salir
        JButton btnSalir = new JButton("Salir");
        btnSalir.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnSalir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnSalir.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSalir.setBackground(new Color(220, 80, 80));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFocusPainted(false);
        btnSalir.setBorderPainted(false);
        btnSalir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSalir.addActionListener(e -> confirmarSalida());
        sidebar.add(btnSalir);

        return sidebar;
    }

    private JButton botonNav(String texto, String tarjeta) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(245, 245, 245));
        btn.setForeground(Color.DARK_GRAY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            cardLayout.show(panelContenido, tarjeta);
            if ("TRABAJOS".equals(tarjeta))   vistaTrabajos.cargarTabla();
            if ("CATEGORIAS".equals(tarjeta)) vistaCategorias.cargarTabla();
            if ("EVENTOS".equals(tarjeta))    vistaEventos.cargarTabla();
        });
        return btn;
    }

    // ----- Panel bienvenida -------------------------------------------------

    private JPanel crearPanelBienvenida() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("Gestor de Trabajos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel bienvenido = new JLabel("Bienvenido, " + Sesion.get().getUsuario().getNombre());
        bienvenido.setFont(new Font("SansSerif", Font.PLAIN, 15));
        bienvenido.setForeground(Color.GRAY);
        bienvenido.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instruccion = new JLabel("Selecciona una opcion del menu de la izquierda.");
        instruccion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        instruccion.setForeground(Color.LIGHT_GRAY);
        instruccion.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel pie = new JLabel("UTS - Programacion Orientada a Objetos | Grupo E194");
        pie.setFont(new Font("SansSerif", Font.PLAIN, 11));
        pie.setForeground(Color.LIGHT_GRAY);
        pie.setAlignmentX(Component.CENTER_ALIGNMENT);

        centro.add(titulo);
        centro.add(Box.createVerticalStrut(10));
        centro.add(bienvenido);
        centro.add(Box.createVerticalStrut(6));
        centro.add(instruccion);
        centro.add(Box.createVerticalStrut(30));
        centro.add(pie);

        p.add(centro);
        return p;
    }

    // ----- Panel reportes ---------------------------------------------------

    private JPanel crearPanelReportes() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Reportes y Estadisticas");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        p.add(titulo, BorderLayout.NORTH);

        JButton btnActualizar = new JButton("Actualizar estadisticas");
        btnActualizar.addActionListener(e -> mostrarEstadisticas(p));
        p.add(btnActualizar, BorderLayout.SOUTH);

        mostrarEstadisticas(p);
        return p;
    }

    private void mostrarEstadisticas(JPanel contenedor) {
        gestornotas.db.TrabajoDAO   trabajoDAO = new gestornotas.db.TrabajoDAO();
        gestornotas.db.CategoriaDAO catDAO     = new gestornotas.db.CategoriaDAO();
        gestornotas.db.EventoDAO    eventoDAO  = new gestornotas.db.EventoDAO();

        int totalTrabajos = trabajoDAO.listar().size();
        int totalCats     = catDAO.listar().size();
        int totalEventos  = eventoDAO.listar().size();

        JPanel stats = new JPanel(new GridLayout(1, 3, 15, 0));
        stats.setBackground(Color.WHITE);
        stats.add(tarjetaEstadistica("Trabajos",   totalTrabajos, new Color(70, 130, 180)));
        stats.add(tarjetaEstadistica("Categorias", totalCats,     new Color(60, 179, 113)));
        stats.add(tarjetaEstadistica("Eventos",    totalEventos,  new Color(210, 105, 30)));

        if (contenedor.getComponentCount() > 2) contenedor.remove(1);
        contenedor.add(stats, BorderLayout.CENTER);
        contenedor.revalidate();
        contenedor.repaint();
    }

    private JPanel tarjetaEstadistica(String etiqueta, int valor, Color color) {
        JPanel t = new JPanel(new GridBagLayout());
        t.setBackground(color);
        t.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(color);

        JLabel lblValor = new JLabel(String.valueOf(valor), SwingConstants.CENTER);
        lblValor.setFont(new Font("SansSerif", Font.BOLD, 36));
        lblValor.setForeground(Color.WHITE);
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNombre = new JLabel(etiqueta, SwingConstants.CENTER);
        lblNombre.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(lblValor);
        inner.add(Box.createVerticalStrut(5));
        inner.add(lblNombre);
        t.add(inner);
        return t;
    }

    // ----- Confirmar salida -------------------------------------------------

    private void confirmarSalida() {
        int op = JOptionPane.showConfirmDialog(
            this,
            "Deseas cerrar el Gestor de Trabajos?",
            "Confirmar salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (op == JOptionPane.YES_OPTION) {
            Conexion.cerrar();
            System.exit(0);
        }
    }
}