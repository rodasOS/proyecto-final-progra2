package videoclub.ui;

import videoclub.dao.ClienteDao;
import videoclub.model.Cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

class ClientesPanel extends JPanel implements Refrescable {
    private final ClienteDao dao = new ClienteDao();
    private final JTextField codigo = new JTextField(12), nombre = new JTextField(30), direccion = new JTextField(30),
        telefono = new JTextField(14), email = new JTextField(24), buscar = new JTextField(18);
    private final JComboBox<String> estado = new JComboBox<>(new String[]{"Activo", "Inactivo"});
    private final DefaultTableModel modelo = new DefaultTableModel(
        new String[]{"Codigo", "Nombre", "Direccion", "Telefono", "Email", "Estado"}, 0);
    private final JTable tabla = Ui.tabla(modelo);

    ClientesPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel form = Ui.formulario(new String[]{"Codigo", "Nombre", "Direccion", "Telefono", "Email", "Estado"},
            new JComponent[]{codigo, nombre, direccion, telefono, email, estado});
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(Ui.boton("Guardar nuevo", this::insertar));
        botones.add(Ui.boton("Modificar", this::modificar));
        botones.add(Ui.boton("Limpiar", this::limpiar));
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER); norte.add(botones, BorderLayout.SOUTH);

        JPanel busq = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busq.add(new JLabel("Consultar:")); busq.add(buscar);
        busq.add(Ui.boton("Buscar", this::refrescar));
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(busq, BorderLayout.NORTH); centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH); add(centro, BorderLayout.CENTER);

        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) cargarSeleccion(); });
        refrescar();
    }

    private Cliente leer() {
        return new Cliente(codigo.getText().trim(), nombre.getText().trim(), direccion.getText().trim(),
            telefono.getText().trim(), email.getText().trim(), estado.getSelectedIndex() == 0 ? "A" : "I");
    }

    private void insertar() {
        try { dao.insertar(leer()); Ui.info(this, "Cliente guardado."); limpiar(); refrescar(); }
        catch (Exception e) { Ui.error(this, e); }
    }

    private void modificar() {
        try { dao.actualizar(leer()); Ui.info(this, "Cliente actualizado."); refrescar(); }
        catch (Exception e) { Ui.error(this, e); }
    }

    private void limpiar() {
        for (JTextField f : new JTextField[]{codigo, nombre, direccion, telefono, email}) f.setText("");
        estado.setSelectedIndex(0); tabla.clearSelection();
    }

    private void cargarSeleccion() {
        int r = tabla.getSelectedRow(); if (r < 0) return;
        r = tabla.convertRowIndexToModel(r);
        codigo.setText(String.valueOf(modelo.getValueAt(r, 0))); nombre.setText(String.valueOf(modelo.getValueAt(r, 1)));
        direccion.setText(String.valueOf(modelo.getValueAt(r, 2))); telefono.setText(String.valueOf(modelo.getValueAt(r, 3)));
        email.setText(String.valueOf(modelo.getValueAt(r, 4)));
        estado.setSelectedIndex("Activo".equals(modelo.getValueAt(r, 5)) ? 0 : 1);
    }

    @Override public void refrescar() {
        try {
            modelo.setRowCount(0);
            for (Cliente c : dao.listar(buscar.getText()))
                modelo.addRow(new Object[]{c.codigo(), c.nombre(), c.direccion(), c.telefono(), c.email(),
                    "A".equals(c.estado()) ? "Activo" : "Inactivo"});
        } catch (Exception e) { Ui.error(this, e); }
    }
}
