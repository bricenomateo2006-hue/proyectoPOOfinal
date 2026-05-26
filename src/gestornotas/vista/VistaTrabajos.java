package gestornotas.vista;

import gestornotas.db.CategoriaDAO;
import gestornotas.db.TrabajoDAO;
import gestornotas.db.UsuarioDAO;
import gestornotas.modelo.Categoria;
import gestornotas.modelo.Trabajo;
import gestornotas.modelo.Usuario;
import gestornotas.util.Sesion;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VistaTrabajos extends JPanel {

    private final TrabajoDAO   dao        = new TrabajoDAO();
    private final UsuarioDAO   usuarioDAO = new UsuarioDAO();
    private final CategoriaDAO catDAO     = new CategoriaDAO();

    private final DefaultTableModel    modeloTabla;
    private final JTable               tabla;
    private final JTextField           txtBuscar = new JTextField(20);

    // Campos del formulario (administrador)
    private final JTextField           txtSolicitante  = new JTextField(25);
    private final JTextField           txtBeneficiario = new JTextField(25);
    private final JTextField           txtOficina      = new JTextField(25);
    private final JTextField           txtFecha        = new JTextField(12);
    private final JTextArea            txtDescripcion  = new JTextArea(3, 30);
    private final JComboBox<Usuario>   cmbAsignado     = new JComboBox<>();
    private final JComboBox<Categoria> cmbCategoria    = new JComboBox<>();
    private final JCheckBox            chkUrgente      = new JCheckBox("Marcar como URGENTE");

    // Panel de informacion (usuario)
    private final JTextArea txtInfo        = new JTextArea(8, 40);
    private final JButton   btnSolucionado = new JButton("Marcar como Solucionado");
    private final JButton   btnRechazar    = new JButton("Rechazar servicio");

    // Botones del formulario admin
    private final JButton btnGuardar  = new JButton("Guardar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar  = new JButton("Limpiar");

    private int idEditando = -1;

    // Colores de estado para las filas
    private static final Color COL_URGENTE     = new Color(255, 210, 210);
    private static final Color COL_SOLUCIONADO = new Color(210, 240, 220);
    private static final Color COL_RECHAZADO   = new Color(240, 215, 215);
    private static final Color COL_PENDIENTE   = new Color(255, 248, 210);

    public VistaTrabajos() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Titulo
        JLabel titulo = new JLabel("Gestion de Trabajos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(titulo, BorderLayout.NORTH);

        // Columnas segun rol
        String[] cols = Sesion.get().esAdmin()
            ? new String[]{"ID", "Solicitante", "Beneficiario", "Oficina", "Fecha", "Categoria", "Estado", "Urgente", "Asignado a"}
            : new String[]{"ID", "Solicitante", "Beneficiario", "Oficina", "Fecha", "Categoria", "Estado", "Urgente"};

        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabla.setRowHeight(24);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tabla.getColumnModel().getColumn(0).setMaxWidth(45);
        tabla.getColumnModel().getColumn(7).setMaxWidth(65);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0)
                cargarEnFormulario(tabla.getSelectedRow());
        });

        // Renderer de colores por estado
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                if (!sel) {
                    Object estado   = t.getValueAt(row, 6);
                    Object urgente  = t.getValueAt(row, 7);
                    boolean esUrg   = "Si".equals(urgente);
                    if ("SOLUCIONADO".equals(estado))      setBackground(COL_SOLUCIONADO);
                    else if ("RECHAZADO".equals(estado))   setBackground(COL_RECHAZADO);
                    else if (esUrg)                        setBackground(COL_URGENTE);
                    else                                   setBackground(COL_PENDIENTE);
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(0, 210));

        // Barra de busqueda
        JPanel barraBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barraBusqueda.setBackground(Color.WHITE);
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodos  = new JButton("Mostrar todos");
        btnBuscar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnTodos.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnBuscar.addActionListener(e -> buscar());
        btnTodos.addActionListener(e  -> { txtBuscar.setText(""); cargarTabla(); });
        barraBusqueda.add(lblBuscar);
        barraBusqueda.add(txtBuscar);
        barraBusqueda.add(btnBuscar);
        barraBusqueda.add(btnTodos);

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBackground(Color.WHITE);
        panelTabla.add(barraBusqueda, BorderLayout.NORTH);
        panelTabla.add(scroll,        BorderLayout.CENTER);

        JPanel panelInferior = Sesion.get().esAdmin()
            ? crearFormularioAdmin()
            : crearPanelUsuario();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelTabla, panelInferior);
        split.setDividerLocation(250);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        if (Sesion.get().esAdmin()) {
            cargarComboUsuarios();
            cargarComboCategorias();
        }
        cargarTabla();
    }

    // ----- Formulario administrador -----------------------------------------

    private JPanel crearFormularioAdmin() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Formulario de Trabajo"));

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        Font f = new Font("SansSerif", Font.PLAIN, 13);
        for (JTextField tf : new JTextField[]{txtSolicitante, txtBeneficiario, txtOficina, txtFecha})
            tf.setFont(f);
        txtDescripcion.setFont(f);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        cmbAsignado.setFont(f);
        cmbCategoria.setFont(f);
        chkUrgente.setFont(f);
        chkUrgente.setBackground(Color.WHITE);

        // Fila 0: Solicitante | Beneficiario
        etiqueta("Solicitante (obligatorio):", gbc, 0, 0, campos, f);
        campo(txtSolicitante, gbc, 1, 0, campos);
        etiqueta("Beneficiario (obligatorio):", gbc, 2, 0, campos, f);
        campo(txtBeneficiario, gbc, 3, 0, campos);

        // Fila 1: Oficina | Fecha
        etiqueta("Oficina (obligatorio):", gbc, 0, 1, campos, f);
        campo(txtOficina, gbc, 1, 1, campos);
        etiqueta("Fecha (yyyy-MM-dd, obligatorio):", gbc, 2, 1, campos, f);
        campo(txtFecha, gbc, 3, 1, campos);

        // Fila 2: Asignado a | Categoria
        etiqueta("Asignar a (obligatorio):", gbc, 0, 2, campos, f);
        campo(cmbAsignado, gbc, 1, 2, campos);
        etiqueta("Categoria:", gbc, 2, 2, campos, f);
        campo(cmbCategoria, gbc, 3, 2, campos);

        // Fila 3: Urgente checkbox
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=4; gbc.weightx=1;
        campos.add(chkUrgente, gbc);
        gbc.gridwidth=1;

        // Fila 4: Descripcion
        etiqueta("Descripcion:", gbc, 0, 4, campos, f);
        gbc.gridx=1; gbc.gridy=4; gbc.gridwidth=3; gbc.weightx=1;
        gbc.fill=GridBagConstraints.BOTH; gbc.weighty=1;
        campos.add(new JScrollPane(txtDescripcion), gbc);
        gbc.gridwidth=1; gbc.weighty=0; gbc.fill=GridBagConstraints.HORIZONTAL;

        panel.add(campos, BorderLayout.CENTER);

        // Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        botones.setBackground(Color.WHITE);

        estiloPrincipal(btnGuardar,  new Color(60, 179, 113));
        estiloNeutro(btnLimpiar);
        btnEliminar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnEliminar.setBackground(new Color(220, 80, 80));
        btnEliminar.setForeground(Color.BLACK);
        btnEliminar.setFocusPainted(false);

        btnGuardar.addActionListener(e  -> guardarAdmin());
        btnEliminar.addActionListener(e -> eliminarAdmin());
        btnLimpiar.addActionListener(e  -> limpiarFormulario());

        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        botones.add(btnGuardar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    // ----- Panel usuario ----------------------------------------------------

    private JPanel crearPanelUsuario() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Detalle del Trabajo"));

        txtInfo.setEditable(false);
        txtInfo.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtInfo.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JScrollPane(txtInfo), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        botones.setBackground(Color.WHITE);

        estiloPrincipal(btnSolucionado, new Color(60, 179, 113));
        btnRechazar.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btnRechazar.setBackground(new Color(220, 80, 80));
        btnRechazar.setForeground(Color.BLACK);
        btnRechazar.setFocusPainted(false);

        btnSolucionado.setEnabled(false);
        btnRechazar.setEnabled(false);
        btnSolucionado.addActionListener(e -> cambiarEstadoUsuario("SOLUCIONADO"));
        btnRechazar.addActionListener(e    -> cambiarEstadoUsuario("RECHAZADO"));

        botones.add(btnRechazar);
        botones.add(btnSolucionado);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    // ----- Carga de datos ---------------------------------------------------

    public void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Trabajo t : dao.listar()) agregarFila(t);
        limpiarFormulario();
    }

    private void agregarFila(Trabajo t) {
        String urgStr = t.isUrgente() ? "Si" : "No";
        if (Sesion.get().esAdmin()) {
            modeloTabla.addRow(new Object[]{
                t.getId(), t.getSolicitante(), t.getBeneficiario(), t.getOficina(),
                t.getFechaPostulacion() != null ? t.getFechaPostulacion().toString() : "",
                t.getCategoriaNombre(), t.getEstado(), urgStr, t.getAsignadoNombre()
            });
        } else {
            modeloTabla.addRow(new Object[]{
                t.getId(), t.getSolicitante(), t.getBeneficiario(), t.getOficina(),
                t.getFechaPostulacion() != null ? t.getFechaPostulacion().toString() : "",
                t.getCategoriaNombre(), t.getEstado(), urgStr
            });
        }
    }

    private void cargarComboUsuarios() {
        cmbAsignado.removeAllItems();
        for (Usuario u : usuarioDAO.listar())
            if (!"ADMIN".equals(u.getRolNombre())) cmbAsignado.addItem(u);
    }

    private void cargarComboCategorias() {
        cmbCategoria.removeAllItems();
        cmbCategoria.addItem(new Categoria(0, "Sin categoria", "", "TRABAJO", null));
        for (Categoria c : catDAO.listarPorTipo("TRABAJO")) cmbCategoria.addItem(c);
    }

    private void cargarEnFormulario(int fila) {
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Trabajo t = dao.buscarPorId(id);
        if (t == null) return;
        idEditando = t.getId();

        if (Sesion.get().esAdmin()) {
            txtSolicitante.setText(t.getSolicitante());
            txtBeneficiario.setText(t.getBeneficiario());
            txtOficina.setText(t.getOficina());
            txtFecha.setText(t.getFechaPostulacion() != null ? t.getFechaPostulacion().toString() : "");
            txtDescripcion.setText(t.getDescripcion() != null ? t.getDescripcion() : "");
            chkUrgente.setSelected(t.isUrgente());
            for (int i = 0; i < cmbAsignado.getItemCount(); i++)
                if (cmbAsignado.getItemAt(i).getId() == t.getAsignadoId()) { cmbAsignado.setSelectedIndex(i); break; }
            for (int i = 0; i < cmbCategoria.getItemCount(); i++)
                if (cmbCategoria.getItemAt(i).getId() == t.getCategoriaId()) { cmbCategoria.setSelectedIndex(i); break; }
        } else {
            String urgStr = t.isUrgente() ? "  [URGENTE]" : "";
            String info = String.format(
                "ID Trabajo   : %d%s%n" +
                "Estado       : %s%n" +
                "Categoria    : %s%n%n" +
                "Solicitante  : %s%n" +
                "Beneficiario : %s%n" +
                "Oficina      : %s%n" +
                "Fecha Post.  : %s%n%n" +
                "Descripcion  :%n%s%n%n" +
                "Creado por   : %s%n" +
                "Registrado   : %s",
                t.getId(), urgStr, t.getEstado(), t.getCategoriaNombre(),
                t.getSolicitante(), t.getBeneficiario(), t.getOficina(),
                t.getFechaPostulacion() != null ? t.getFechaPostulacion().toString() : "-",
                t.getDescripcion() != null ? t.getDescripcion() : "-",
                t.getCreadoPorNombre(),
                t.getCreadoEn() != null ? t.getCreadoEn().toString().substring(0, 16) : "-"
            );
            txtInfo.setText(info);
            boolean pendiente = t.isPendiente();
            btnSolucionado.setEnabled(pendiente);
            btnRechazar.setEnabled(pendiente);
        }
    }

    // ----- Acciones ---------------------------------------------------------

    private void guardarAdmin() {
        String sol = txtSolicitante.getText().trim();
        String ben = txtBeneficiario.getText().trim();
        String ofi = txtOficina.getText().trim();
        String fec = txtFecha.getText().trim();

        if (sol.isEmpty() || ben.isEmpty() || ofi.isEmpty() || fec.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Solicitante, Beneficiario, Oficina y Fecha son obligatorios.",
                "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cmbAsignado.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay usuarios disponibles para asignar.",
                "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Date fecha;
        try {
            fecha = Date.valueOf(LocalDate.parse(fec));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Formato de fecha invalido. Use yyyy-MM-dd.",
                "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Usuario   asignado = (Usuario)   cmbAsignado.getSelectedItem();
        Categoria cat      = (Categoria) cmbCategoria.getSelectedItem();
        if (asignado == null) return;

        Trabajo t = new Trabajo();
        t.setSolicitante(sol);
        t.setBeneficiario(ben);
        t.setOficina(ofi);
        t.setFechaPostulacion(fecha);
        t.setDescripcion(txtDescripcion.getText().trim());
        t.setUrgente(chkUrgente.isSelected());
        t.setAsignadoId(asignado.getId());
        t.setCategoriaId(cat != null && cat.getId() > 0 ? cat.getId() : 0);

        boolean ok;
        if (idEditando > 0) {
            t.setId(idEditando); ok = dao.actualizar(t);
            if (ok) JOptionPane.showMessageDialog(this, "Trabajo actualizado.");
        } else {
            ok = dao.insertar(t);
            if (ok) JOptionPane.showMessageDialog(this, "Trabajo creado y asignado.");
        }
        if (ok) { limpiarFormulario(); cargarTabla(); }
        else JOptionPane.showMessageDialog(this, "Error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void eliminarAdmin() {
        if (idEditando <= 0) { JOptionPane.showMessageDialog(this, "Selecciona un trabajo primero."); return; }
        int op = JOptionPane.showConfirmDialog(this,
            "Eliminar este trabajo permanentemente?",
            "Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            if (dao.eliminar(idEditando)) {
                JOptionPane.showMessageDialog(this, "Trabajo eliminado.");
                limpiarFormulario(); cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cambiarEstadoUsuario(String nuevoEstado) {
        if (idEditando <= 0) { JOptionPane.showMessageDialog(this, "Selecciona un trabajo primero."); return; }
        String msg = "SOLUCIONADO".equals(nuevoEstado)
            ? "Marcar este trabajo como SOLUCIONADO?"
            : "Rechazar este trabajo?";
        if (JOptionPane.showConfirmDialog(this, msg, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (dao.cambiarEstado(idEditando, nuevoEstado)) {
                JOptionPane.showMessageDialog(this, "Trabajo marcado como " + nuevoEstado + ".");
                limpiarFormulario(); cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo actualizar. El trabajo podria no estar pendiente.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void buscar() {
        String termino = txtBuscar.getText().trim();
        if (termino.isEmpty()) { cargarTabla(); return; }
        modeloTabla.setRowCount(0);
        for (Trabajo t : dao.buscar(termino)) agregarFila(t);
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        idEditando = -1;
        if (Sesion.get().esAdmin()) {
            txtSolicitante.setText(""); txtBeneficiario.setText(""); txtOficina.setText("");
            txtFecha.setText(LocalDate.now().toString()); txtDescripcion.setText("");
            chkUrgente.setSelected(false);
            if (cmbAsignado.getItemCount()  > 0) cmbAsignado.setSelectedIndex(0);
            if (cmbCategoria.getItemCount() > 0) cmbCategoria.setSelectedIndex(0);
        } else {
            txtInfo.setText("Selecciona un trabajo de la lista para ver sus detalles.");
            btnSolucionado.setEnabled(false);
            btnRechazar.setEnabled(false);
        }
        tabla.clearSelection();
    }

    // ----- Helpers de layout ------------------------------------------------

    private void etiqueta(String texto, GridBagConstraints gbc, int x, int y,
                          JPanel panel, Font f) {
        JLabel l = new JLabel(texto);
        l.setFont(f);
        gbc.gridx = x; gbc.gridy = y; gbc.weightx = 0;
        panel.add(l, gbc);
    }

    private void campo(JComponent comp, GridBagConstraints gbc, int x, int y, JPanel panel) {
        gbc.gridx = x; gbc.gridy = y; gbc.weightx = 1;
        panel.add(comp, gbc);
    }

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