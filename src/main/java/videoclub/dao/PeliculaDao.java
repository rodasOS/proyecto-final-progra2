package videoclub.dao;

import videoclub.db.Database;
import videoclub.db.NegocioException;
import videoclub.model.Pelicula;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a la tabla de peliculas. */
public class PeliculaDao {
    private static final int ANIO_MINIMO = 1888;
    private static final int ANIO_MAXIMO = 2100;

    private static final String SQL_INSERTAR =
        "INSERT INTO pelicula (codigo, titulo, anio, duracion, clasificacion, actores, director, ejemplares) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
        "UPDATE pelicula SET titulo = ?, anio = ?, duracion = ?, clasificacion = ?, actores = ?, director = ?, "
        + "ejemplares = ? WHERE codigo = ?";
    private static final String SQL_BUSCAR = "SELECT * FROM pelicula WHERE codigo = ?";
    private static final String SQL_LISTAR =
        "SELECT * FROM pelicula WHERE codigo LIKE ? OR titulo LIKE ? OR director LIKE ? OR actores LIKE ? "
        + "ORDER BY titulo";
    private static final String SQL_EXISTE = "SELECT 1 FROM pelicula WHERE codigo = ?";

    public void insertar(Pelicula pelicula) throws NegocioException {
        validar(pelicula);
        try (Connection c = Database.conectar()) {
            if (existe(c, pelicula.codigo())) {
                throw new NegocioException("Ya existe una película con código " + pelicula.codigo() + ".");
            }
            try (PreparedStatement ps = c.prepareStatement(SQL_INSERTAR)) {
                ps.setString(1, pelicula.codigo());
                ps.setString(2, pelicula.titulo());
                ps.setInt(3, pelicula.anio());
                ps.setInt(4, pelicula.duracion());
                ps.setString(5, pelicula.clasificacion());
                ps.setString(6, pelicula.actores());
                ps.setString(7, pelicula.director());
                ps.setInt(8, pelicula.ejemplares());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw NegocioException.deBaseDeDatos(e);
        }
    }

    public void actualizar(Pelicula pelicula) throws NegocioException {
        validar(pelicula);
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, pelicula.titulo());
            ps.setInt(2, pelicula.anio());
            ps.setInt(3, pelicula.duracion());
            ps.setString(4, pelicula.clasificacion());
            ps.setString(5, pelicula.actores());
            ps.setString(6, pelicula.director());
            ps.setInt(7, pelicula.ejemplares());
            ps.setString(8, pelicula.codigo());
            if (ps.executeUpdate() == 0) {
                throw new NegocioException("No existe la película " + pelicula.codigo() + ".");
            }
        } catch (SQLException e) {
            throw NegocioException.deBaseDeDatos(e);
        }
    }

    public Optional<Pelicula> buscar(String codigo) throws NegocioException {
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(SQL_BUSCAR)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw NegocioException.deBaseDeDatos(e);
        }
    }

    /** Busca por codigo, titulo, director o actores. Un filtro vacio devuelve todas. */
    public List<Pelicula> listar(String filtro) throws NegocioException {
        String patron = "%" + (filtro == null ? "" : filtro.trim()) + "%";
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(SQL_LISTAR)) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, patron);
            }
            List<Pelicula> peliculas = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    peliculas.add(mapear(rs));
                }
            }
            return peliculas;
        } catch (SQLException e) {
            throw NegocioException.deBaseDeDatos(e);
        }
    }

    private boolean existe(Connection c, String codigo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_EXISTE)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void validar(Pelicula pelicula) throws NegocioException {
        if (pelicula.codigo().isBlank() || pelicula.titulo().isBlank()) {
            throw new NegocioException("Código y título son obligatorios.");
        }
        if (pelicula.anio() < ANIO_MINIMO || pelicula.anio() > ANIO_MAXIMO) {
            throw new NegocioException("El año no es válido.");
        }
        if (pelicula.duracion() <= 0) {
            throw new NegocioException("La duración debe ser mayor a 0.");
        }
        if (pelicula.ejemplares() < 0) {
            throw new NegocioException("Los ejemplares no pueden ser negativos.");
        }
    }

    private static Pelicula mapear(ResultSet rs) throws SQLException {
        return new Pelicula(
            rs.getString("codigo"),
            rs.getString("titulo"),
            rs.getInt("anio"),
            rs.getInt("duracion"),
            rs.getString("clasificacion"),
            rs.getString("actores"),
            rs.getString("director"),
            rs.getInt("ejemplares"));
    }
}
