package videoclub.ui;

import videoclub.dao.ClienteDao;
import videoclub.db.NegocioException;
import videoclub.model.Cliente;
import videoclub.model.Estado;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.Objects;

/** Pantalla para registrar, modificar y consultar clientes. */
class ClientesPanel extends JPanel implements Refrescable {
    private static final String ETIQUETA_ACTIVO = "Activo";
    private static final String ETIQUETA_INACTIVO = "Inactivo";
    private static final String[] COLUMNAS = {"Código", "Nombre", "Dirección", "Teléfono", "Email", "Estado"};

    private final ClienteDao dao = new ClienteDao();
    private final JTextField campoCodigo = new JTextField(12);
    private final JTextField campoNombre = new JTextField(30);
    private final JTextField campoDireccion = new JTextField(30);
    private final JTextField campoTelefono = new JTextField(14);
    private final JTextField campoEmail = new JTextField(24);
    private final JComboBox<String> comboEstado = new JComboBox<>(new String[]{ETIQUETA_ACTIVO, ETIQUETA_INACTIVO});
    private final JTextField campoBuscar = new JTextField(18);
    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0);
    private final JTable tabla = Ui.tabla(modelo);

    ClientesPanel() {
        super(new BorderLayout(8, 8));
        Ui.aplicarMargen(this, 10);

        JPanel formulario = Ui.formulario(
            new String[]{"Código", "Nombre", "Dirección", "Teléfono", "Email", "Estado"},
            new JComponent[]{campoCodigo, campoNombre, campoDireccion, campoTelefono, campoEmail, comboEstado});

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(formulario, BorderLayout.CENTER);
        norte.add(Ui.barra(
            Ui.boton("Guardar nuevo", this::insertar),
            Ui.boton("Modificar", this::modificar),
            Ui.boton("Limpiar", this::limpiar)), BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(Ui.barra(
            new JLabel("Consultar:"),
            campoBuscar,
            Ui.boton("Buscar", this::refrescar)), BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        refrescar();
    }

    private Cliente leerFormulario() {
        return new Cliente(
            campoCodigo.getText().trim(),
            campoNombre.getText().trim(),
            campoDireccion.getText().trim(),
            campoTelefono.getText().trim(),
            campoEmail.getText().trim(),
            codigoDeEstado((String) comboEstado.getSelectedItem()));
    }

    private void insertar() {
        try {
            dao.insertar(leerFormulario());
            Ui.mostrarInfo(this, "Cliente guardado.");
            limpiar();
            refrescar();
        } catch (NegocioException e) {
            Ui.mostrarError(this, e);
        }
    }

    private void modificar() {
        try {
            dao.actualizar(leerFormulario());
            Ui.mostrarInfo(this, "Cliente actualizado.");
            refrescar();
        } catch (NegocioException e) {
            Ui.mostrarError(this, e);
        }
    }

    private void limpiar() {
        for (JTextField campo : new JTextField[]{campoCodigo, campoNombre, campoDireccion, campoTelefono, campoEmail}) {
            campo.setText("");
        }
        comboEstado.setSelectedIndex(0);
        tabla.clearSelection();
    }

    /** Copia la fila seleccionada a los campos para poder modificarla. */
    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        campoCodigo.setText(valor(filaModelo, 0));
        campoNombre.setText(valor(filaModelo, 1));
        campoDireccion.setText(valor(filaModelo, 2));
        campoTelefono.setText(valor(filaModelo, 3));
        campoEmail.setText(valor(filaModelo, 4));
        comboEstado.setSelectedItem(valor(filaModelo, 5));
    }

    private String valor(int fila, int columna) {
        return Objects.toString(modelo.getValueAt(fila, columna), "");
    }

    @Override
    public void refrescar() {
        try {
            modelo.setRowCount(0);
            for (Cliente c : dao.listar(campoBuscar.getText())) {
                modelo.addRow(new Object[]{c.codigo(), c.nombre(), c.direccion(), c.telefono(), c.email(),
                    etiquetaDeEstado(c.estado())});
            }
        } catch (NegocioException e) {
            Ui.mostrarError(this, e);
        }
    }

    private static String etiquetaDeEstado(String estado) {
        return Estado.ACTIVO.equals(estado) ? ETIQUETA_ACTIVO : ETIQUETA_INACTIVO;
    }

    private static String codigoDeEstado(String etiqueta) {
        return ETIQUETA_ACTIVO.equals(etiqueta) ? Estado.ACTIVO : Estado.INACTIVO;
    }
}
