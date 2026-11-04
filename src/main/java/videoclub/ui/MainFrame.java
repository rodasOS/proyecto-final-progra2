package videoclub.ui;

import videoclub.db.Database;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        super("Videoclub - Control de peliculas, clientes y prestamos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);

        JLabel titulo = new JLabel("  VIDEOCLUB", SwingConstants.LEFT);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setForeground(Color.WHITE);
        titulo.setOpaque(true); titulo.setBackground(Ui.PRIMARIO);
        titulo.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));

        PeliculasPanel peliculas = new PeliculasPanel();
        ClientesPanel clientes = new ClientesPanel();
        PrestamosPanel prestamos = new PrestamosPanel();
        DevolucionesPanel devoluciones = new DevolucionesPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Peliculas", peliculas);
        tabs.addTab("Clientes", clientes);
        tabs.addTab("Prestamos", prestamos);
        tabs.addTab("Devoluciones", devoluciones);
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof Refrescable r) r.refrescar();
        });

        JLabel estado = new JLabel("  Base de datos: " + Database.motor());
        estado.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        setLayout(new BorderLayout());
        add(titulo, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
    }
}
