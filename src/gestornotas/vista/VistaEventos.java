package gestornotas.vista;

import gestornotas.db.CategoriaDAO;
import gestornotas.db.EventoDAO;
import gestornotas.modelo.Categoria;
import gestornotas.modelo.Evento;
import gestornotas.util.Sesion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VistaEventos extends JPanel {

    private final EventoDAO    dao    = new EventoDAO();
    private final CategoriaDAO catDAO = new CategoriaDAO();

    private final DefaultTableModel    modeloTabla;
    private final JTable               tabla;

    // Campos del formulario
    private final JTextField           txtNombre      = new JTextField(30);
    private final JTextArea            txtDescripcion = new JTextArea(3, 30);
    private final JTextField           txtFecha       = new JTextField(12);
    private final JComboBox<String>    cmbTipo        = new JComboBox<>();
    private final JComboBox<Categoria> cmbCategoria   = new JComboBox<>();

    // Botones
    private final JButton btnGuardar     = new JButton("Guardar");
    private final JButton btnEliminar    = new JButton("Eliminar");
    private final JButton btnSilenciar   = new JButton("Silenciar");
    private final JButton btnDesilenciar = new JButton("Activar");
    private final JButton btnLimpiar     = new JButton("Limpiar");
    private final JCheckBox chkVerTodos  = new JCheckBox("Mostrar eventos silenciados");

    private int idEditando = -1;

    public VistaEventos() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Titulo
        JLabel titulo = new JLabel("Gestion de Eventos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(titulo, BorderLayout.NORTH);

        // Opciones del combo tipo segun rol
        if (Sesion.get().esAdmin()) {
            cmbTipo.addItem("PERSONAL");
            cmbTipo.addItem("GLOBAL");
        } else {
            cmbTipo.addItem("PERSONAL");
        }

        cargarComboCategorias();

        // Tabla
        String[] cols = {"ID", "Nombre", "Fecha", "Categoria", "Tipo", "Creador", "Silenciado"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabla.setRowHeight(24);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tabla.getColumnModel().getColumn(0).setMaxWidth(45);
        tabla.getColumnModel().getColumn(6).setMaxWidth(80);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0)
                cargarEnFormulario(tabla.getSelectedRow());
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(0, 210));

        // Barra encima de la tabla
        JPanel barraTabla = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barraTabla.setBackground(Color.WHITE);
        chkVerTodos.setFont(new Font("SansSerif", Font.PLAIN, 13));
        chkVerTodos.setBackground(Color.WHITE);
        chkVerTodos.addActionListener(e -> cargarTabla());
        barraTabla.add(chkVerTodos);
        if (!Sesion.get().esAdmin()) {
            JLabel info = new JLabel("Solo puedes crear eventos de tipo PERSONAL");
            info.setFont(new Font("SansSerif", Font.ITALIC, 11));
            info.setForeground(Color.GRAY);
            barraTabla.add(info);
        }

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBackground(Color.WHITE);
        panelTabla.add(barraTabla, BorderLayout.NORTH);
        panelTabla.add(scroll,     BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelTabla, crearFormulario());
        split.setDividerLocation(250);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        cargarTabla();
    }

    // ----- Formulario -------------------------------------------------------

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Formulario de Evento"));

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 8, 5, 8);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        Font fuenteLabel = new Font("SansSerif", Font.PLAIN, 13);
        Font fuenteCampo = new Font("SansSerif", Font.PLAIN, 13);

        JLabel lblNombre   = new JLabel("Nombre (obligatorio):");
        JLabel lblFecha    = new JLabel("Fecha (yyyy-MM-dd, obligatorio):");
        JLabel lblTipo     = Sesion.get().esAdmin()
            ? new JLabel("Tipo  [solo administradores pueden elegir GLOBAL]:")
            : new JLabel("Tipo:");
        JLabel lblCat      = new JLabel("Categoria:");
        JLabel lblDesc     = new JLabel("Descripcion:");

        for (JLabel l : new JLabel[]{lblNombre, lblFecha, lblTipo, lblCat, lblDesc})
            l.setFont(fuenteLabel);
        txtNombre.setFont(fuenteCampo);
        txtFecha.setFont(fuenteCampo);
        cmbTipo.setFont(fuenteCampo);
        cmbCategoria.setFont(fuenteCampo);
        txtDescripcion.setFont(fuenteCampo);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        // Nombre
        gbc.gridx=0; gbc.gridy=0; gbc.weightx=0; campos.add(lblNombre, gbc);
        gbc.gridx=1; gbc.weightx=1;               campos.add(txtNombre, gbc);
        // Fecha
        gbc.gridx=0; gbc.gridy=1; gbc.weightx=0; campos.add(lblFecha, gbc);
        gbc.gridx=1; gbc.weightx=1;               campos.add(txtFecha, gbc);
        // Tipo
        gbc.gridx=0; gbc.gridy=2; gbc.weightx=0; campos.add(lblTipo, gbc);
        gbc.gridx=1; gbc.weightx=1;               campos.add(cmbTipo, gbc);
        // Categoria
        gbc.gridx=0; gbc.gridy=3; gbc.weightx=0; campos.add(lblCat, gbc);
        gbc.gridx=1; gbc.weightx=1;               campos.add(cmbCategoria, gbc);
        // Descripcion
        gbc.gridx=0; gbc.gridy=4; gbc.weightx=0; gbc.anchor=GridBagConstraints.NORTHWEST;
        campos.add(lblDesc, gbc);
        gbc.gridx=1; gbc.weightx=1; gbc.fill=GridBagConstraints.BOTH; gbc.weighty=1;
        campos.add(new JScrollPane(txtDescripcion), gbc);

        panel.add(campos, BorderLayout.CENTER);

        // Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        botones.setBackground(Color.WHITE);

        estiloPrincipal(btnGuardar,     new Color(60, 179, 113));
        estiloNeutro(btnEliminar);
        btnEliminar.setBackground(new Color(220, 80, 80));
        btnEliminar.setForeground(Color.BLACK);
        estiloPrincipal(btnSilenciar,   new Color(120, 100, 180));
        estiloPrincipal(btnDesilenciar, new Color(70, 130, 180));
        estiloNeutro(btnLimpiar);

        btnGuardar.addActionListener(e     -> guardar());
        btnEliminar.addActionListener(e    -> eliminar());
        btnSilenciar.addActionListener(e   -> silenciar());
        btnDesilenciar.addActionListener(e -> desilenciar());
        btnLimpiar.addActionListener(e     -> limpiarFormulario());

        botones.add(btnDesilenciar);
        botones.add(btnSilenciar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        botones.add(btnGuardar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    // ----- Carga publica ----------------------------------------------------

    public void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Evento> eventos = chkVerTodos.isSelected() ? dao.listarTodos() : dao.listar();
        for (Evento ev : eventos) {
            modeloTabla.addRow(new Object[]{
                ev.getId(), ev.getNombre(),
                ev.getFecha() != null ? ev.getFecha().toString() : "",
                ev.getCategoriaNombre(), ev.getTipo(), ev.getUsuarioNombre(),
                ev.isSilenciado() ? "Si" : "No"
            });
        }
        limpiarFormulario();
    }

    private void cargarComboCategorias() {
        cmbCategoria.removeAllItems();
        cmbCategoria.addItem(new Categoria(0, "Sin categoria", "", "EVENTO", null));
        for (Categoria c : catDAO.listarPorTipo("EVENTO")) cmbCategoria.addItem(c);
    }

    private void cargarEnFormulario(int fila) {
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Evento ev = dao.buscarPorId(id);
        if (ev == null) return;
        idEditando = ev.getId();
        txtNombre.setText(ev.getNombre());
        txtDescripcion.setText(ev.getDescripcion() != null ? ev.getDescripcion() : "");
        txtFecha.setText(ev.getFecha() != null ? ev.getFecha().toString() : "");
        if (Sesion.get().esAdmin()) cmbTipo.setSelectedItem(ev.getTipo());
        for (int i = 0; i < cmbCategoria.getItemCount(); i++)
            if (cmbCategoria.getItemAt(i).getId() == ev.getCategoriaId()) {
                cmbCategoria.setSelectedIndex(i); break;
            }

        boolean esMio       = ev.esMio(Sesion.get().getUsuarioId());
        boolean esAdmin     = Sesion.get().esAdmin();
        boolean puedeEditar = esMio || esAdmin;
        boolean silenciado  = ev.isSilenciado();

        btnGuardar.setEnabled(puedeEditar);
        btnEliminar.setEnabled(puedeEditar);
        cmbTipo.setEnabled(esAdmin);
        if (!esMio) {
            btnSilenciar.setEnabled(!silenciado);
            btnDesilenciar.setEnabled(silenciado);
        } else if ("GLOBAL".equals(ev.getTipo())) {
            btnSilenciar.setEnabled(!silenciado);
            btnDesilenciar.setEnabled(silenciado);
        } else {
            btnSilenciar.setEnabled(false);
            btnDesilenciar.setEnabled(false);
        }
    }

    // ----- Acciones ---------------------------------------------------------

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Date fecha;
        try {
            fecha = Date.valueOf(LocalDate.parse(txtFecha.getText().trim()));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha invalido. Use yyyy-MM-dd.", "Validacion", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tipo = (String) cmbTipo.getSelectedItem();
        if (!Sesion.get().esAdmin() && "GLOBAL".equals(tipo)) {
            JOptionPane.showMessageDialog(this,
                "Solo el administrador puede crear eventos GLOBALES.",
                "Permiso denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Categoria cat = (Categoria) cmbCategoria.getSelectedItem();

        Evento ev = new Evento();
        ev.setNombre(nombre);
        ev.setDescripcion(txtDescripcion.getText().trim());
        ev.setFecha(fecha);
        ev.setTipo(tipo != null ? tipo : "PERSONAL");
        ev.setCategoriaId(cat != null && cat.getId() > 0 ? cat.getId() : 0);

        boolean ok;
        if (idEditando > 0) {
            ev.setId(idEditando); ok = dao.actualizar(ev);
            if (ok) JOptionPane.showMessageDialog(this, "Evento actualizado.");
        } else {
            ok = dao.insertar(ev);
            if (ok) JOptionPane.showMessageDialog(this, "Evento creado.");
        }
        if (ok) { limpiarFormulario(); cargarTabla(); }
        else JOptionPane.showMessageDialog(this, "Error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void eliminar() {
        if (idEditando <= 0) { JOptionPane.showMessageDialog(this, "Selecciona un evento primero."); return; }
        String msg = Sesion.get().esAdmin()
            ? "Eliminar este evento para todos los usuarios?"
            : "Eliminar este evento?";
        if (JOptionPane.showConfirmDialog(this, msg, "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            if (dao.eliminar(idEditando)) {
                JOptionPane.showMessageDialog(this, "Evento eliminado.");
                limpiarFormulario(); cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void silenciar() {
        if (idEditando <= 0) { JOptionPane.showMessageDialog(this, "Selecciona un evento primero."); return; }
        if (dao.silenciar(idEditando)) {
            JOptionPane.showMessageDialog(this, "Evento silenciado.");
            limpiarFormulario(); cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "Error al silenciar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desilenciar() {
        if (idEditando <= 0) { JOptionPane.showMessageDialog(this, "Selecciona un evento silenciado primero."); return; }
        if (dao.desilenciar(idEditando)) {
            JOptionPane.showMessageDialog(this, "Evento activado.");
            limpiarFormulario(); cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "Error al activar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        idEditando = -1;
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtFecha.setText(LocalDate.now().toString());
        cmbTipo.setSelectedIndex(0);
        cmbCategoria.setSelectedIndex(0);
        tabla.clearSelection();
        btnGuardar.setEnabled(true);
        btnEliminar.setEnabled(true);
        btnSilenciar.setEnabled(false);
        btnDesilenciar.setEnabled(false);
        cmbTipo.setEnabled(Sesion.get().esAdmin());
    }

    // ----- Estilo de botones ------------------------------------------------

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