package videoclub.dao;

import videoclub.db.Database;
import videoclub.db.NegocioException;
import videoclub.model.Pelicula;

import java.sql.*;
import java.util.*;

public class PeliculaDao {
    public void insertar(Pelicula p) throws NegocioException {
        validar(p);
        String sql = "INSERT INTO pelicula VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (existe(c, p.codigo())) throw new NegocioException("Ya existe una pelicula con codigo " + p.codigo());
            ps.setString(1, p.codigo()); ps.setString(2, p.titulo()); ps.setInt(3, p.anio());
            ps.setInt(4, p.duracion()); ps.setString(5, p.clasificacion()); ps.setString(6, p.actores());
            ps.setString(7, p.director()); ps.setInt(8, p.ejemplares());
            ps.executeUpdate();
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    public void actualizar(Pelicula p) throws NegocioException {
        validar(p);
        String sql = "UPDATE pelicula SET titulo=?,anio=?,duracion=?,clasificacion=?,actores=?,director=?,ejemplares=? WHERE codigo=?";
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.titulo()); ps.setInt(2, p.anio()); ps.setInt(3, p.duracion());
            ps.setString(4, p.clasificacion()); ps.setString(5, p.actores()); ps.setString(6, p.director());
            ps.setInt(7, p.ejemplares()); ps.setString(8, p.codigo());
            if (ps.executeUpdate() == 0) throw new NegocioException("No existe la pelicula " + p.codigo());
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    public Optional<Pelicula> buscar(String codigo) throws NegocioException {
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement("SELECT * FROM pelicula WHERE codigo=?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    public List<Pelicula> listar(String filtro) throws NegocioException {
        String sql = "SELECT * FROM pelicula WHERE codigo LIKE ? OR titulo LIKE ? OR director LIKE ? OR actores LIKE ? ORDER BY titulo";
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            String f = "%" + (filtro == null ? "" : filtro.trim()) + "%";
            for (int i = 1; i <= 4; i++) ps.setString(i, f);
            List<Pelicula> r = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) r.add(map(rs)); }
            return r;
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    private boolean existe(Connection c, String codigo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM pelicula WHERE codigo=?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private void validar(Pelicula p) throws NegocioException {
        if (p.codigo().isBlank() || p.titulo().isBlank()) throw new NegocioException("Codigo y titulo son obligatorios.");
        if (p.anio() < 1888 || p.anio() > 2100) throw new NegocioException("El anio no es valido.");
        if (p.duracion() <= 0) throw new NegocioException("La duracion debe ser mayor a 0.");
        if (p.ejemplares() < 0) throw new NegocioException("Los ejemplares no pueden ser negativos.");
    }

    private Pelicula map(ResultSet rs) throws SQLException {
        return new Pelicula(rs.getString("codigo"), rs.getString("titulo"), rs.getInt("anio"), rs.getInt("duracion"),
            rs.getString("clasificacion"), rs.getString("actores"), rs.getString("director"), rs.getInt("ejemplares"));
    }
}
