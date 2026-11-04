package videoclub.ui;

import videoclub.dao.*;
import videoclub.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

class PrestamosPanel extends JPanel implements Refrescable {
    private final PrestamoDao dao = new PrestamoDao();
    private final PeliculaDao peliculas = new PeliculaDao();
    private final ClienteDao clientes = new ClienteDao();
    private final JComboBox<Pelicula> cmbPelicula = new JComboBox<>();
    private final JComboBox<Cliente> cmbCliente = new JComboBox<>();
    private final JCheckBox soloActivos = new JCheckBox("Solo activos", true);
    private final DefaultTableModel modelo = new DefaultTableModel(
        new String[]{"Prestamo", "Pelicula", "Cliente", "Fecha prestamo", "Fecha devolucion", "Estado"}, 0);
    private final JTable tabla = Ui.tabla(modelo);

    PrestamosPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cmbPelicula.setRenderer((l, v, i, s, f) -> new DefaultListCellRenderer().getListCellRendererComponent(
            l, v == null ? "" : v.codigo() + " - " + v.titulo(), i, s, f));
        cmbCliente.setRenderer((l, v, i, s, f) -> new DefaultListCellRenderer().getListCellRendererComponent(
            l, v == null ? "" : v.codigo() + " - " + v.nombre(), i, s, f));
        JPanel form = Ui.formulario(new String[]{"Pelicula", "Cliente"}, new JComponent[]{cmbPelicula, cmbCliente});
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(Ui.boton("Registrar prestamo", this::prestar));
        botones.add(soloActivos);
        soloActivos.addActionListener(e -> cargarTabla());
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER); norte.add(botones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH); add(new JScrollPane(tabla), BorderLayout.CENTER);
        refrescar();
    }

    private void prestar() {
        Pelicula p = (Pelicula) cmbPelicula.getSelectedItem();
        Cliente c = (Cliente) cmbCliente.getSelectedItem();
        if (p == null || c == null) { Ui.info(this, "Seleccione una pelicula y un cliente."); return; }
        try {
            int id = dao.prestar(p.codigo(), c.codigo());
            Ui.info(this, "Prestamo registrado. Codigo de prestamo: " + id);
            cargarTabla();
        } catch (Exception e) { Ui.error(this, e); }
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            for (Prestamo p : dao.listar(soloActivos.isSelected()))
                modelo.addRow(new Object[]{p.codigo(), p.codigoPelicula(), p.codigoCliente(), p.fechaPrestamo(),
                    p.fechaDevolucion() == null ? "" : p.fechaDevolucion(), p.estado()});
        } catch (Exception e) { Ui.error(this, e); }
    }

    @Override public void refrescar() {
        try {
            cmbPelicula.removeAllItems(); cmbCliente.removeAllItems();
            for (Pelicula p : peliculas.listar("")) cmbPelicula.addItem(p);
            for (Cliente c : clientes.listar("")) if ("A".equals(c.estado())) cmbCliente.addItem(c);
        } catch (Exception e) { Ui.error(this, e); }
        cargarTabla();
    }
}
