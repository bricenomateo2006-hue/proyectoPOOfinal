package gestornotas.vista;

import gestornotas.db.CategoriaDAO;
import gestornotas.modelo.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Vista con dos pestanas: categorias de TRABAJOS y categorias de EVENTOS.
 */
public class VistaCategorias extends JPanel {

    private final CategoriaDAO dao = new CategoriaDAO();

    // Pestana TRABAJOS
    private final DefaultTableModel modeloTrabajos;
    private final JTable            tablaTrabajos;
    private final JTextField        txtNombreT      = new JTextField(25);
    private final JTextField        txtDescripcionT = new JTextField(35);
    private int idEditandoT = -1;

    // Pestana EVENTOS
    private final DefaultTableModel modeloEventos;
    private final JTable            tablaEventos;
    private final JTextField        txtNombreE      = new JTextField(25);
    private final JTextField        txtDescripcionE = new JTextField(35);
    private int idEditandoE = -1;

    public VistaCategorias() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Titulo de la seccion
        JLabel titulo = new JLabel("Gestion de Categorias");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(titulo, BorderLayout.NORTH);

        // Columnas de la tabla
        String[] cols = {"ID", "Nombre", "Descripcion", "Creado en"};

        modeloTrabajos = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaTrabajos = crearTabla(modeloTrabajos, true);

        modeloEventos = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEventos = crearTabla(modeloEventos, false);

        // Pestanas
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabs.addTab("Para Trabajos", crearPestana(tablaTrabajos, txtNombreT, txtDescripcionT, true));
        tabs.addTab("Para Eventos",  crearPestana(tablaEventos,  txtNombreE, txtDescripcionE, false));

        add(tabs, BorderLayout.CENTER);
        cargarTabla();
    }

    private JTable crearTabla(DefaultTableModel modelo, boolean esTrabajo) {
        JTable t = new JTable(modelo);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setRowHeight(24);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        t.getColumnModel().getColumn(0).setMaxWidth(50);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && t.getSelectedRow() >= 0)
                cargarEnFormulario(t.getSelectedRow(), esTrabajo);
        });
        return t;
    }

    private JPanel crearPestana(JTable tabla, JTextField txtNombre,
                                JTextField txtDesc, boolean esTrabajo) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(0, 200));

        JPanel formulario = crearFormulario(txtNombre, txtDesc, esTrabajo);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, formulario);
        split.setDividerLocation(210);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormulario(JTextField txtNombre, JTextField txtDesc, boolean esTrabajo) {
        String tituloFormulario = esTrabajo ? "Nueva categoria de Trabajo" : "Nueva categoria de Evento";

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(tituloFormulario));

        // Campos
        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 8, 6, 8);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        JLabel lblNombre = new JLabel("Nombre (obligatorio):");
        lblNombre.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtNombre.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel lblDesc = new JLabel("Descripcion:");
        lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtDesc.setFont(new Font("SansSerif", Font.PLAIN, 13));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; campos.add(lblNombre, gbc);
        gbc.gridx = 1; gbc.weightx = 1;                 campos.add(txtNombre, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; campos.add(lblDesc,   gbc);
        gbc.gridx = 1; gbc.weightx = 1;                 campos.add(txtDesc,   gbc);

        panel.add(campos, BorderLayout.CENTER);

        // Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        botones.setBackground(Color.WHITE);

        JButton btnGuardar  = new JButton("Guardar");
        JButton btnLimpiar  = new JButton("Limpiar");
        JButton btnEliminar = new JButton("Eliminar");

        estiloPrincipal(btnGuardar,  new Color(60, 179, 113));
        estiloNeutro(btnLimpiar);
        estiloNeutro(btnEliminar);
        btnEliminar.setBackground(new Color(220, 80, 80));
        btnEliminar.setForeground(Color.BLACK);

        btnGuardar.addActionListener(e  -> guardar(txtNombre, txtDesc, esTrabajo));
        btnLimpiar.addActionListener(e  -> limpiar(txtNombre, txtDesc, esTrabajo));
        btnEliminar.addActionListener(e -> eliminar(esTrabajo));

        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        botones.add(btnGuardar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    // ----- Carga publica (MenuPrincipal la llama al navegar) ----------------

    public void cargarTabla() {
        modeloTrabajos.setRowCount(0);
        for (Categoria c : dao.listarPorTipo("TRABAJO")) {
            modeloTrabajos.addRow(new Object[]{
                c.getId(), c.getNombre(), c.getDescripcion(),
                c.getCreadoEn() != null ? c.getCreadoEn().toString().substring(0, 16) : ""
            });
        }
        modeloEventos.setRowCount(0);
        for (Categoria c : dao.listarPorTipo("EVENTO")) {
            modeloEventos.addRow(new Object[]{
                c.getId(), c.getNombre(), c.getDescripcion(),
                c.getCreadoEn() != null ? c.getCreadoEn().toString().substring(0, 16) : ""
            });
        }
    }

    private void cargarEnFormulario(int fila, boolean esTrabajo) {
        DefaultTableModel modelo = esTrabajo ? modeloTrabajos : modeloEventos;
        JTextField txtN = esTrabajo ? txtNombreT : txtNombreE;
        JTextField txtD = esTrabajo ? txtDescripcionT : txtDescripcionE;

        int id = (int) modelo.getValueAt(fila, 0);
        Categoria c = dao.buscarPorId(id);
        if (c == null) return;

        if (esTrabajo) idEditandoT = c.getId();
        else           idEditandoE = c.getId();

        txtN.setText(c.getNombre());
        txtD.setText(c.getDescripcion() != null ? c.getDescripcion() : "");
    }

    private void guardar(JTextField txtN, JTextField txtD, boolean esTrabajo) {
        String nombre = txtN.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El nombre es obligatorio.", "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idE = esTrabajo ? idEditandoT : idEditandoE;
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setDescripcion(txtD.getText().trim());
        c.setTipo(esTrabajo ? "TRABAJO" : "EVENTO");

        boolean ok;
        if (idE > 0) {
            c.setId(idE);
            ok = dao.actualizar(c);
            if (ok) JOptionPane.showMessageDialog(this, "Categoria actualizada.");
        } else {
            ok = dao.insertar(c);
            if (ok) JOptionPane.showMessageDialog(this, "Categoria creada.");
        }
        if (ok) { limpiar(txtN, txtD, esTrabajo); cargarTabla(); }
        else JOptionPane.showMessageDialog(this, "Error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void eliminar(boolean esTrabajo) {
        int idE = esTrabajo ? idEditandoT : idEditandoE;
        if (idE <= 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una categoria primero.");
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
            "Eliminar esta categoria?\nLos registros asociados quedaran sin categoria.",
            "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            if (dao.eliminar(idE)) {
                JOptionPane.showMessageDialog(this, "Categoria eliminada.");
                limpiar(esTrabajo ? txtNombreT : txtNombreE,
                        esTrabajo ? txtDescripcionT : txtDescripcionE, esTrabajo);
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiar(JTextField txtN, JTextField txtD, boolean esTrabajo) {
        if (esTrabajo) { idEditandoT = -1; tablaTrabajos.clearSelection(); }
        else           { idEditandoE = -1; tablaEventos.clearSelection();  }
        txtN.setText(""); txtD.setText("");
    }

    // ----- Metodos de estilo de botones -------------------------------------

    private void estiloPrincipal(JButton btn, Color color) {
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
    }

    private void estiloNeutro(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setFocusPainted(false);
    }
}