package videoclub.ui;

import videoclub.dao.PrestamoDao;
import videoclub.model.Prestamo;

import javax.swing.*;
import java.awt.*;

class DevolucionesPanel extends JPanel implements Refrescable {
    private final PrestamoDao dao = new PrestamoDao();
    private final JTextField codigo = new JTextField(10);
    private final JLabel detalle = new JLabel(" ");

    DevolucionesPanel() {
        super(new FlowLayout(FlowLayout.LEFT, 12, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila.add(new JLabel("Codigo de prestamo:")); fila.add(codigo);
        fila.add(Ui.boton("Consultar", this::consultar));
        fila.add(Ui.boton("Registrar devolucion", this::devolver));
        col.add(fila); col.add(detalle);
        add(col);
    }

    private void consultar() {
        try {
            Prestamo p = dao.buscar(Ui.entero("Codigo de prestamo", codigo.getText()))
                .orElseThrow(() -> new videoclub.db.NegocioException("No existe ese prestamo."));
            detalle.setText("Pelicula " + p.codigoPelicula() + " | Cliente " + p.codigoCliente()
                + " | Prestado " + p.fechaPrestamo() + " | Estado " + p.estado());
        } catch (Exception e) { detalle.setText(" "); Ui.error(this, e); }
    }

    private void devolver() {
        try {
            dao.devolver(Ui.entero("Codigo de prestamo", codigo.getText()));
            Ui.info(this, "Devolucion registrada.");
            consultar();
        } catch (Exception e) { Ui.error(this, e); }
    }

    @Override public void refrescar() { }
}
