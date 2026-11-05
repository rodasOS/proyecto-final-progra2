package videoclub.ui;

import videoclub.db.NegocioException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Utilidades visuales compartidas por todas las pantallas. */
final class Ui {
    static final Color COLOR_PRIMARIO = new Color(0x1F3A5F);

    private static final int ALTO_FILA = 24;

    private Ui() { }

    static void mostrarError(Component padre, Exception e) {
        boolean esAviso = e instanceof NegocioException;
        JOptionPane.showMessageDialog(padre, e.getMessage(),
            esAviso ? "Aviso" : "Error",
            esAviso ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    static void mostrarInfo(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    static JButton boton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.addActionListener(evento -> accion.run());
        return boton;
    }

    /** Fila de componentes alineados a la izquierda. */
    static JPanel barra(JComponent... componentes) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JComponent componente : componentes) {
            barra.add(componente);
        }
        return barra;
    }

    static int enteroDe(String campo, String texto) throws NegocioException {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new NegocioException("\"" + campo + "\" debe ser un número entero.");
        }
    }

    /** Formulario de dos columnas: etiqueta a la izquierda y campo a la derecha. */
    static JPanel formulario(String[] etiquetas, JComponent[] campos) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(4, 6, 4, 6);
        restricciones.anchor = GridBagConstraints.WEST;
        for (int fila = 0; fila < etiquetas.length; fila++) {
            restricciones.gridx = 0;
            restricciones.gridy = fila;
            restricciones.fill = GridBagConstraints.NONE;
            restricciones.weightx = 0;
            panel.add(new JLabel(etiquetas[fila] + ":"), restricciones);

            restricciones.gridx = 1;
            restricciones.fill = GridBagConstraints.HORIZONTAL;
            restricciones.weightx = 1;
            panel.add(campos[fila], restricciones);
        }
        return panel;
    }

    /** Tabla de solo lectura, con encabezado coloreado y orden por columna. */
    static JTable tabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla.setRowHeight(ALTO_FILA);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setDefaultRenderer(crearRenderizadorEncabezado());
        return tabla;
    }

    /**
     * Pinta el encabezado con colores propios. Windows ignora setBackground/setForeground
     * del JTableHeader y dibuja el texto blanco sobre fondo claro, por eso no se veia.
     */
    private static DefaultTableCellRenderer crearRenderizadorEncabezado() {
        DefaultTableCellRenderer renderizador = new DefaultTableCellRenderer();
        renderizador.setOpaque(true);
        renderizador.setBackground(COLOR_PRIMARIO);
        renderizador.setForeground(Color.WHITE);
        renderizador.setHorizontalAlignment(SwingConstants.LEFT);
        renderizador.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        return renderizador;
    }

    /** Margen interior uniforme para los paneles principales. */
    static void aplicarMargen(JPanel panel, int margen) {
        panel.setBorder(BorderFactory.createEmptyBorder(margen, margen, margen, margen));
    }
}
