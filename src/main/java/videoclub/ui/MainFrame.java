package videoclub.ui;

import videoclub.db.Database;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/** Ventana principal: encabezado, una pestana por seccion y barra de estado. */
public class MainFrame extends JFrame {
    private static final String TITULO = "Videoclub - Control de películas, clientes y préstamos";

    public MainFrame() {
        super(TITULO);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearPestanas(), BorderLayout.CENTER);
        add(crearBarraDeEstado(), BorderLayout.SOUTH);
    }

    private static JLabel crearEncabezado() {
        JLabel encabezado = new JLabel("  VIDEOCLUB", SwingConstants.LEFT);
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 22f));
        encabezado.setForeground(Color.WHITE);
        encabezado.setOpaque(true);
        encabezado.setBackground(Ui.COLOR_PRIMARIO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        return encabezado;
    }

    private static JTabbedPane crearPestanas() {
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Películas", new PeliculasPanel());
        pestanas.addTab("Clientes", new ClientesPanel());
        pestanas.addTab("Préstamos", new PrestamosPanel());
        pestanas.addTab("Devoluciones", new DevolucionesPanel());
        pestanas.addChangeListener(evento -> {
            if (pestanas.getSelectedComponent() instanceof Refrescable pantalla) {
                pantalla.refrescar();
            }
        });
        return pestanas;
    }

    private static JLabel crearBarraDeEstado() {
        JLabel barra = new JLabel("  Base de datos: " + Database.motor());
        barra.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return barra;
    }
}
