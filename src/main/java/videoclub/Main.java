package videoclub;

import videoclub.db.Database;
import videoclub.ui.CaratulaDialog;
import videoclub.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada: prepara la base de datos y abre la ventana principal. */
public class Main {

    public static void main(String[] args) {
        usarAspectoDelSistema();
        try {
            Database.iniciar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "No se pudo iniciar la base de datos:\n" + e.getMessage(),
                "Error fatal", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(Database::detener));
        SwingUtilities.invokeLater(Main::mostrarVentanaPrincipal);
    }

    /** Abre la ventana principal y, encima, la caratula modal del proyecto. */
    private static void mostrarVentanaPrincipal() {
        MainFrame ventana = new MainFrame();
        ventana.setVisible(true);
        new CaratulaDialog(ventana).setVisible(true);
    }

    private static void usarAspectoDelSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si no se puede, se usa el aspecto por defecto de Swing.
        }
    }
}
