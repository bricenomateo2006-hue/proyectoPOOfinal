package gestornotas.vista;

import gestornotas.db.UsuarioDAO;
import gestornotas.modelo.Usuario;
import gestornotas.util.Sesion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Pantalla de inicio de sesion.
 */
public class VentanaLogin extends JFrame {

    private final JTextField     txtEmail    = new JTextField(25);
    private final JPasswordField txtPassword = new JPasswordField(25);
    private final UsuarioDAO     usuarioDAO  = new UsuarioDAO();

    public VentanaLogin() {
        setTitle("Gestor de Trabajos - Iniciar Sesion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(400, 400);
        setLocationRelativeTo(null);

        // Panel principal con fondo blanco
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Titulo
        JLabel lblTitulo = new JLabel("Gestor de Trabajos");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("UTS - POO Grupo E194");
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitulo.setForeground(Color.GRAY);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        root.add(lblTitulo);
        root.add(Box.createVerticalStrut(5));
        root.add(lblSubtitulo);
        root.add(Box.createVerticalStrut(20));

        // Separador
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        root.add(sep);
        root.add(Box.createVerticalStrut(20));

        // Campo email
        JLabel lblEmail = new JLabel("Correo electronico:");
        lblEmail.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Campo contrasena
        JLabel lblPass = new JLabel("Contrasena:");
        lblPass.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        root.add(lblEmail);
        root.add(Box.createVerticalStrut(4));
        root.add(txtEmail);
        root.add(Box.createVerticalStrut(10));
        root.add(lblPass);
        root.add(Box.createVerticalStrut(4));
        root.add(txtPassword);
        root.add(Box.createVerticalStrut(20));

        // Ingresar con Enter
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) intentarLogin();
            }
        });

        // Botones
        JButton btnLogin = new JButton("Iniciar Sesion");
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnLogin.setBackground(new Color(70, 130, 180));
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFocusPainted(false);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnRegistro = new JButton("No tengo cuenta - Registrarme");
        btnRegistro.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnRegistro.setBorderPainted(false);
        btnRegistro.setBackground(Color.WHITE);
        btnRegistro.setForeground(new Color(70, 130, 180));
        btnRegistro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRegistro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnRegistro.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnLogin.addActionListener(e    -> intentarLogin());
        btnRegistro.addActionListener(e -> abrirRegistro());

        root.add(btnLogin);
        root.add(Box.createVerticalStrut(8));
        root.add(btnRegistro);

        setContentPane(root);
    }

    private void intentarLogin() {
        String email = txtEmail.getText().trim();
        String pass  = new String(txtPassword.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Completa todos los campos.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario u = usuarioDAO.login(email, pass);
        if (u != null) {
            Sesion.get().setUsuario(u);
            new MenuPrincipal().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Credenciales incorrectas o cuenta inactiva.",
                "Error", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
        }
    }

    private void abrirRegistro() {
        new VentanaRegistro(this).setVisible(true);
    }
}