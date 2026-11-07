package videoclub.ui;

import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;

/** Caratula del proyecto. Se muestra como ventana modal al iniciar la aplicacion. */
public final class CaratulaDialog extends JDialog {
    private static final String UNIVERSIDAD = "UNIVERSIDAD MARIANO GÁLVEZ DE GUATEMALA";
    private static final String FACULTAD = "Facultad de Ingeniería en Sistemas";
    private static final String CURSO = "Curso: Programación II";
    private static final String PROYECTO = "Proyecto Final: Sistema de Videoclub";
    private static final String RUTA_LOGO = "/logo-umg.png";
    private static final int ALTO_LOGO = 130;

    private record Integrante(String nombre, String carnet) { }

    private static final Integrante[] INTEGRANTES = {
        new Integrante("Mario Orlando Sales Rodas", "2990-25-21488"),
        new Integrante("Edgar Aldanis Chávez Estrada", "2990-25-19528"),
        new Integrante("Kenneth Gabriel Elias Xia Temal", "2990-25-16687"),
        new Integrante("Erick Adolfo de León Rivera", "2990-25-13618"),
    };

    public CaratulaDialog(Frame duenio) {
        super(duenio, "Proyecto Final", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contenido.add(crearLogo());
        contenido.add(Box.createVerticalStrut(12));
        contenido.add(centrado(texto(UNIVERSIDAD, 15, Font.BOLD)));
        contenido.add(centrado(texto(FACULTAD, 13, Font.PLAIN)));
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(centrado(texto(CURSO, 13, Font.PLAIN)));
        contenido.add(centrado(texto(PROYECTO, 13, Font.PLAIN)));
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(centrado(texto("Integrantes", 13, Font.BOLD)));
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(crearTablaIntegrantes());
        contenido.add(Box.createVerticalStrut(18));
        contenido.add(centrado(crearBotonContinuar()));

        setLayout(new BorderLayout());
        add(contenido, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(duenio);
    }

    private static JLabel crearLogo() {
        ImageIcon original = new ImageIcon(CaratulaDialog.class.getResource(RUTA_LOGO));
        Image escalada = original.getImage().getScaledInstance(-1, ALTO_LOGO, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(escalada));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        return logo;
    }

    private static JLabel texto(String texto, int tamanio, int estilo) {
        JLabel etiqueta = new JLabel(texto, SwingConstants.CENTER);
        etiqueta.setFont(etiqueta.getFont().deriveFont(estilo, tamanio));
        return etiqueta;
    }

    private static JPanel centrado(Component componente) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.add(componente);
        return panel;
    }

    private static JPanel crearTablaIntegrantes() {
        JPanel tabla = new JPanel(new GridLayout(0, 2, 20, 4));
        for (Integrante integrante : INTEGRANTES) {
            tabla.add(new JLabel(integrante.nombre()));
            tabla.add(new JLabel(integrante.carnet()));
        }
        tabla.setPreferredSize(new Dimension(420, tabla.getPreferredSize().height));
        return centrado(tabla);
    }

    private JButton crearBotonContinuar() {
        JButton boton = new JButton("Continuar");
        boton.addActionListener(evento -> dispose());
        getRootPane().setDefaultButton(boton);
        return boton;
    }
}
