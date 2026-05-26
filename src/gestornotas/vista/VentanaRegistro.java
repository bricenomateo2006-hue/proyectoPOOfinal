package gestornotas.vista;

import gestornotas.db.UsuarioDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Ventana modal de registro de nuevo usuario.
 */
public class VentanaRegistro extends JDialog {

    private final JTextField     txtNombre   = new JTextField(25);
    private final JTextField     txtEmail    = new JTextField(25);
    private final JPasswordField txtPassword = new JPasswordField(25);
    private final JPasswordField txtConfirm  = new JPasswordField(25);
    private final UsuarioDAO     usuarioDAO  = new UsuarioDAO();

    public VentanaRegistro(JFrame parent) {
        super(parent, "Crear Cuenta", true);
        setResizable(false);
        setSize(600, 600);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));

        // Titulo
        JLabel lblTitulo = new JLabel("Crear Cuenta Nueva");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("UTS - POO Grupo E194");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        root.add(lblTitulo);
        root.add(Box.createVerticalStrut(4));
        root.add(lblSub);
        root.add(Box.createVerticalStrut(15));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        root.add(sep);
        root.add(Box.createVerticalStrut(15));

        // Campos del formulario
        agregarCampo(root, "Nombre completo:", txtNombre);
        agregarCampo(root, "Correo electronico:", txtEmail);
        agregarCampo(root, "Contrasena (min. 6 caracteres):", txtPassword);
        agregarCampo(root, "Confirmar contrasena:", txtConfirm);

        // Registrar con Enter
        txtConfirm.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) intentarRegistro();
            }
        });

        root.add(Box.createVerticalStrut(15));

        // Botones
        JButton btnRegistrar = new JButton("Crear Cuenta");
        btnRegistrar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnRegistrar.setBackground(new Color(70, 130, 180));
        btnRegistrar.setForeground(Color.BLACK);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnRegistrar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnVolver = new JButton("Volver al inicio de sesion");
        btnVolver.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnVolver.setBorderPainted(false);
        btnVolver.setBackground(Color.WHITE);
        btnVolver.setForeground(new Color(70, 130, 180));
        btnVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVolver.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnVolver.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnRegistrar.addActionListener(e -> intentarRegistro());
        btnVolver.addActionListener(e    -> dispose());

        root.add(btnRegistrar);
        root.add(Box.createVerticalStrut(8));
        root.add(btnVolver);

        setContentPane(root);
    }

    /** Agrega una etiqueta y su campo al panel de forma uniforme. */
    private void agregarCampo(JPanel panel, String etiqueta, JTextField campo) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(3));
        panel.add(campo);
        panel.add(Box.createVerticalStrut(10));
    }

    private void intentarRegistro() {
        String nombre  = txtNombre.getText().trim();
        String email   = txtEmail.getText().trim();
        String pass    = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirm.getPassword());

        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            aviso("Completa todos los campos.");
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}$")) {
            aviso("El correo electronico no es valido.");
            return;
        }
        if (pass.length() < 6) {
            aviso("La contrasena debe tener al menos 6 caracteres.");
            return;
        }
        if (!pass.equals(confirm)) {
            aviso("Las contrasenas no coinciden.");
            txtConfirm.setText("");
            return;
        }
        if (usuarioDAO.emailExiste(email)) {
            aviso("Ese correo ya esta registrado.");
            return;
        }

        if (usuarioDAO.registrar(nombre, email, pass)) {
            JOptionPane.showMessageDialog(this,
                "Cuenta creada correctamente.\nYa puedes iniciar sesion.",
                "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Error al crear la cuenta. Intenta de nuevo.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}