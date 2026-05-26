package gestornotas;
import gestornotas.vista.VentanaLogin;
import javax.swing.*;

/**
 * Punto de entrada de la aplicación Gestor de Trabajos.
 * UTS — Programación Orientada a Objetos | Grupo E194
 * Autor: Mathius Joel Briceño
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new VentanaLogin().setVisible(true);  // ← cambiado
        });
    }
}