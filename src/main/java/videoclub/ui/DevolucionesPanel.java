package videoclub.ui;

import videoclub.dao.PrestamoDao;
import videoclub.db.NegocioException;
import videoclub.model.Prestamo;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/** Pantalla para consultar un prestamo por su codigo y registrar su devolucion. */
class DevolucionesPanel extends JPanel implements Refrescable {
    private final PrestamoDao dao = new PrestamoDao();
    private final JTextField campoCodigo = new JTextField(10);
    private final JLabel etiquetaDetalle = new JLabel(" ");

    DevolucionesPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(Ui.barra(
            new JLabel("Código de préstamo:"),
            campoCodigo,
            Ui.boton("Consultar", this::consultar),
            Ui.boton("Registrar devolución", this::devolver)), BorderLayout.NORTH);
        add(etiquetaDetalle, BorderLayout.CENTER);
    }

    private void consultar() {
        try {
            Prestamo p = buscarPrestamo();
            etiquetaDetalle.setText("Película " + p.codigoPelicula() + " | Cliente " + p.codigoCliente()
                + " | Prestado el " + p.fechaPrestamo() + " | Estado " + p.estado());
        } catch (NegocioException e) {
            etiquetaDetalle.setText(" ");
            Ui.mostrarError(this, e);
        }
    }

    private void devolver() {
        try {
            dao.devolver(Ui.enteroDe("Código de préstamo", campoCodigo.getText()));
            Ui.mostrarInfo(this, "Devolución registrada.");
            consultar();
        } catch (NegocioException e) {
            Ui.mostrarError(this, e);
        }
    }

    private Prestamo buscarPrestamo() throws NegocioException {
        int codigo = Ui.enteroDe("Código de préstamo", campoCodigo.getText());
        return dao.buscar(codigo).orElseThrow(() -> new NegocioException("No existe ese préstamo."));
    }

    /** No hay datos que recargar: el usuario consulta por codigo. */
    @Override
    public void refrescar() {
        // Intencionalmente vacio.
    }
}
