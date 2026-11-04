package videoclub;

import videoclub.db.Database;
import videoclub.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
        try {
            Database.iniciar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "No se pudo iniciar la base de datos:\n" + e.getMessage(),
                "Error fatal", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(Database::detener));
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
