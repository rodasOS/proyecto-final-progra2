package videoclub.ui;

import videoclub.dao.PeliculaDao;
import videoclub.model.Pelicula;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

class PeliculasPanel extends JPanel implements Refrescable {
    private final PeliculaDao dao = new PeliculaDao();
    private final JTextField codigo = new JTextField(12), titulo = new JTextField(30), anio = new JTextField(6),
        duracion = new JTextField(6), actores = new JTextField(30), director = new JTextField(20),
        ejemplares = new JTextField(6), buscar = new JTextField(18);
    private final JComboBox<String> clasificacion = new JComboBox<>(new String[]{"G", "PG", "PG-13", "R", "NC-17"});
    private final DefaultTableModel modelo = new DefaultTableModel(
        new String[]{"Codigo", "Titulo", "Anio", "Duracion", "Clasif.", "Actores", "Director", "Ejemplares"}, 0);
    private final JTable tabla = Ui.tabla(modelo);

    PeliculasPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel form = Ui.formulario(
            new String[]{"Codigo", "Titulo", "Anio", "Duracion (min)", "Clasificacion", "Actores (separados por coma)", "Director", "Ejemplares"},
            new JComponent[]{codigo, titulo, anio, duracion, clasificacion, actores, director, ejemplares});
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(Ui.boton("Guardar nueva", this::insertar));
        botones.add(Ui.boton("Modificar", this::modificar));
        botones.add(Ui.boton("Limpiar", this::limpiar));
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER); norte.add(botones, BorderLayout.SOUTH);

        JPanel busq = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busq.add(new JLabel("Consultar:")); busq.add(buscar);
        busq.add(Ui.boton("Buscar", this::refrescar));

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(busq, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH); add(centro, BorderLayout.CENTER);

        tabla.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) cargarSeleccion(); });
        refrescar();
    }

    private Pelicula leer() throws Exception {
        return new Pelicula(codigo.getText().trim(), titulo.getText().trim(), Ui.entero("Anio", anio.getText()),
            Ui.entero("Duracion", duracion.getText()), (String) clasificacion.getSelectedItem(),
            actores.getText().trim(), director.getText().trim(), Ui.entero("Ejemplares", ejemplares.getText()));
    }

    private void insertar() {
        try { dao.insertar(leer()); Ui.info(this, "Pelicula guardada."); limpiar(); refrescar(); }
        catch (Exception e) { Ui.error(this, e); }
    }

    private void modificar() {
        try { dao.actualizar(leer()); Ui.info(this, "Pelicula actualizada."); refrescar(); }
        catch (Exception e) { Ui.error(this, e); }
    }

    private void limpiar() {
        for (JTextField f : new JTextField[]{codigo, titulo, anio, duracion, actores, director, ejemplares}) f.setText("");
        clasificacion.setSelectedIndex(0); tabla.clearSelection();
    }

    private void cargarSeleccion() {
        int r = tabla.getSelectedRow(); if (r < 0) return;
        r = tabla.convertRowIndexToModel(r);
        codigo.setText(String.valueOf(modelo.getValueAt(r, 0))); titulo.setText(String.valueOf(modelo.getValueAt(r, 1)));
        anio.setText(String.valueOf(modelo.getValueAt(r, 2))); duracion.setText(String.valueOf(modelo.getValueAt(r, 3)));
        clasificacion.setSelectedItem(modelo.getValueAt(r, 4)); actores.setText(String.valueOf(modelo.getValueAt(r, 5)));
        director.setText(String.valueOf(modelo.getValueAt(r, 6))); ejemplares.setText(String.valueOf(modelo.getValueAt(r, 7)));
    }

    @Override public void refrescar() {
        try {
            modelo.setRowCount(0);
            for (Pelicula p : dao.listar(buscar.getText()))
                modelo.addRow(new Object[]{p.codigo(), p.titulo(), p.anio(), p.duracion(), p.clasificacion(),
                    p.actores(), p.director(), p.ejemplares()});
        } catch (Exception e) { Ui.error(this, e); }
    }
}
