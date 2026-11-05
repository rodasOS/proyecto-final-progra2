package videoclub.ui;

import videoclub.dao.ClienteDao;
import videoclub.dao.PeliculaDao;
import videoclub.dao.PrestamoDao;
import videoclub.db.NegocioException;
import videoclub.model.Cliente;
import videoclub.model.Estado;
import videoclub.model.Pelicula;
import videoclub.model.Prestamo;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.function.Function;

/** Pantalla para registrar prestamos y consultar los existentes. */
class PrestamosPanel extends JPanel implements Refrescable {
    private static final String[] COLUMNAS = {
        "Préstamo", "Película", "Cliente", "Fecha préstamo", "Fecha devolución", "Estado"};

    private final PrestamoDao prestamoDao = new PrestamoDao();
    private final PeliculaDao peliculaDao = new PeliculaDao();
    private final ClienteDao clienteDao = new ClienteDao();
    private final JComboBox<Pelicula> comboPelicula = new JComboBox<>();
    private final JComboBox<Cliente> comboCliente = new JComboBox<>();
    private final JCheckBox checkSoloActivos = new JCheckBox("Solo activos", true);
    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0);
    private final JTable tabla = Ui.tabla(modelo);

    PrestamosPanel() {
        super(new BorderLayout(8, 8));
        Ui.aplicarMargen(this, 10);

        comboPelicula.setRenderer(renderizador((Pelicula p) -> p.codigo() + " - " + p.titulo()));
        comboCliente.setRenderer(renderizador((Cliente c) -> c.codigo() + " - " + c.nombre()));

        JPanel formulario = Ui.formulario(
            new String[]{"Película", "Cliente"},
            new JComponent[]{comboPelicula, comboCliente});

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(formulario, BorderLayout.CENTER);
        norte.add(Ui.barra(
            Ui.boton("Registrar préstamo", this::prestar),
            checkSoloActivos), BorderLayout.SOUTH);

        checkSoloActivos.addActionListener(evento -> cargarTabla());

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        refrescar();
    }

    /** Muestra cada elemento del combo con el texto que indica la funcion. */
    private static <T> DefaultListCellRenderer renderizador(Function<T, String> textoDe) {
        return new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean enfocado) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, enfocado);
                if (valor != null) {
                    setText(textoDe.apply((T) valor));
                }
                return this;
            }
        };
    }

    private void prestar() {
        Pelicula pelicula = (Pelicula) comboPelicula.getSelectedItem();
        Cliente cliente = (Cliente) comboCliente.getSelectedItem();
        if (pelicula == null || cliente == null) {
            Ui.mostrarInfo(this, "Seleccione una película y un cliente.");
            return;
        }
        try {
            int codigoPrestamo = prestamoDao.prestar(pelicula.codigo(), cliente.codigo());
            Ui.mostrarInfo(this, "Préstamo registrado. Código de préstamo: " + codigoPrestamo);
            cargarTabla();
        } catch (NegocioException e) {
            Ui.mostrarError(this, e);
        }
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            for (Prestamo p : prestamoDao.listar(checkSoloActivos.isSelected())) {
                modelo.addRow(new Object[]{p.codigo(), p.codigoPelicula(), p.codigoCliente(), p.fechaPrestamo(),
                    p.fechaDevolucion() == null ? "" : p.fechaDevolucion(), p.estado()});
            }
        } catch (NegocioException e) {
            Ui.mostrarError(this, e);
        }
    }

    /** Recarga los combos con peliculas y clientes activos, y luego la tabla. */
    @Override
    public void refrescar() {
        try {
            comboPelicula.removeAllItems();
            comboCliente.removeAllItems();
            for (Pelicula p : peliculaDao.listar("")) {
                comboPelicula.addItem(p);
            }
            for (Cliente c : clienteDao.listar("")) {
                if (Estado.ACTIVO.equals(c.estado())) {
                    comboCliente.addItem(c);
                }
            }
        } catch (NegocioException e) {
            Ui.mostrarError(this, e);
        }
        cargarTabla();
    }
}
