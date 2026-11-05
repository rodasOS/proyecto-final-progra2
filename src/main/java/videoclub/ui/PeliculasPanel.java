package videoclub.ui;

import videoclub.dao.PeliculaDao;
import videoclub.db.NegocioException;
import videoclub.model.Pelicula;

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

/** Pantalla para registrar, modificar y consultar peliculas. */
class PeliculasPanel extends JPanel implements Refrescable {
    private static final String[] CLASIFICACIONES = {"G", "PG", "PG-13", "R", "NC-17"};
    private static final String[] COLUMNAS = {
        "Código", "Título", "Año", "Duración", "Clasif.", "Actores", "Director", "Ejemplares"};

    private final PeliculaDao dao = new PeliculaDao();
    private final JTextField campoCodigo = new JTextField(12);
    private final JTextField campoTitulo = new JTextField(30);
    private final JTextField campoAnio = new JTextField(6);
    private final JTextField campoDuracion = new JTextField(6);
    private final JComboBox<String> comboClasificacion = new JComboBox<>(CLASIFICACIONES);
    private final JTextField campoActores = new JTextField(30);
    private final JTextField campoDirector = new JTextField(20);
    private final JTextField campoEjemplares = new JTextField(6);
    private final JTextField campoBuscar = new JTextField(18);
    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0);
    private final JTable tabla = Ui.tabla(modelo);

    PeliculasPanel() {
        super(new BorderLayout(8, 8));
        Ui.aplicarMargen(this, 10);

        JPanel formulario = Ui.formulario(
            new String[]{"Código", "Título", "Año", "Duración (min)", "Clasificación",
                "Actores (separados por coma)", "Director", "Ejemplares"},
            new JComponent[]{campoCodigo, campoTitulo, campoAnio, campoDuracion, comboClasificacion,
                campoActores, campoDirector, campoEjemplares});

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(formulario, BorderLayout.CENTER);
        norte.add(Ui.barra(
            Ui.boton("Guardar nueva", this::insertar),
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

    private Pelicula leerFormulario() throws NegocioException {
        return new Pelicula(
            campoCodigo.getText().trim(),
            campoTitulo.getText().trim(),
            Ui.enteroDe("Año", campoAnio.getText()),
            Ui.enteroDe("Duración", campoDuracion.getText()),
            (String) comboClasificacion.getSelectedItem(),
            campoActores.getText().trim(),
            campoDirector.getText().trim(),
            Ui.enteroDe("Ejemplares", campoEjemplares.getText()));
    }

    private void insertar() {
        try {
            dao.insertar(leerFormulario());
            Ui.mostrarInfo(this, "Película guardada.");
            limpiar();
            refrescar();
        } catch (NegocioException e) {
            Ui.mostrarError(this, e);
        }
    }

    private void modificar() {
        try {
            dao.actualizar(leerFormulario());
            Ui.mostrarInfo(this, "Película actualizada.");
            refrescar();
        } catch (NegocioException e) {
            Ui.mostrarError(this, e);
        }
    }

    private void limpiar() {
        for (JTextField campo : new JTextField[]{campoCodigo, campoTitulo, campoAnio, campoDuracion,
                campoActores, campoDirector, campoEjemplares}) {
            campo.setText("");
        }
        comboClasificacion.setSelectedIndex(0);
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
        campoTitulo.setText(valor(filaModelo, 1));
        campoAnio.setText(valor(filaModelo, 2));
        campoDuracion.setText(valor(filaModelo, 3));
        comboClasificacion.setSelectedItem(valor(filaModelo, 4));
        campoActores.setText(valor(filaModelo, 5));
        campoDirector.setText(valor(filaModelo, 6));
        campoEjemplares.setText(valor(filaModelo, 7));
    }

    private String valor(int fila, int columna) {
        return Objects.toString(modelo.getValueAt(fila, columna), "");
    }

    @Override
    public void refrescar() {
        try {
            modelo.setRowCount(0);
            for (Pelicula p : dao.listar(campoBuscar.getText())) {
                modelo.addRow(new Object[]{p.codigo(), p.titulo(), p.anio(), p.duracion(), p.clasificacion(),
                    p.actores(), p.director(), p.ejemplares()});
            }
        } catch (NegocioException e) {
            Ui.mostrarError(this, e);
        }
    }
}
