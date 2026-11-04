package videoclub.ui;

import videoclub.db.NegocioException;

import javax.swing.*;
import java.awt.*;

final class Ui {
    static final Color PRIMARIO = new Color(0x1F3A5F);
    static final Color ACENTO = new Color(0xE63946);

    private Ui() { }

    static void error(Component p, Exception e) {
        JOptionPane.showMessageDialog(p, e.getMessage(), e instanceof NegocioException ? "Aviso" : "Error", JOptionPane.WARNING_MESSAGE);
    }

    static void info(Component p, String m) { JOptionPane.showMessageDialog(p, m, "Listo", JOptionPane.INFORMATION_MESSAGE); }

    static JButton boton(String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        b.addActionListener(e -> accion.run());
        return b;
    }

    static int entero(String campo, String valor) throws NegocioException {
        try { return Integer.parseInt(valor.trim()); }
        catch (NumberFormatException e) { throw new NegocioException("\"" + campo + "\" debe ser un numero entero."); }
    }

    static JPanel formulario(String[] etiquetas, JComponent[] campos) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6); g.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < etiquetas.length; i++) {
            g.gridx = 0; g.gridy = i; g.fill = GridBagConstraints.NONE; g.weightx = 0;
            p.add(new JLabel(etiquetas[i] + ":"), g);
            g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
            p.add(campos[i], g);
        }
        return p;
    }

    static JTable tabla(javax.swing.table.DefaultTableModel m) {
        JTable t = new JTable(m) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        t.setRowHeight(24);
        t.setAutoCreateRowSorter(true);
        t.getTableHeader().setBackground(PRIMARIO);
        t.getTableHeader().setForeground(Color.WHITE);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return t;
    }
}
