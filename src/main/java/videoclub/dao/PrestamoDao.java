package videoclub.dao;

import videoclub.db.*;
import videoclub.model.Prestamo;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class PrestamoDao {
    /** Registra un prestamo activo. Lanza SinEjemplaresException si no hay copias disponibles. */
    public int prestar(String codPelicula, String codCliente) throws NegocioException {
        try (Connection c = Database.conectar()) {
            c.setAutoCommit(false);
            try {
                String titulo; int ejemplares;
                try (PreparedStatement ps = c.prepareStatement("SELECT titulo, ejemplares FROM pelicula WHERE codigo=? FOR UPDATE")) {
                    ps.setString(1, codPelicula);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new NegocioException("La pelicula " + codPelicula + " no existe.");
                        titulo = rs.getString(1); ejemplares = rs.getInt(2);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT estado FROM cliente WHERE codigo=?")) {
                    ps.setString(1, codCliente);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new NegocioException("El cliente " + codCliente + " no existe.");
                        if (!"A".equals(rs.getString(1))) throw new NegocioException("El cliente esta inactivo y no puede recibir prestamos.");
                    }
                }
                int activos;
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM prestamo WHERE codigo_pelicula=? AND estado='A'")) {
                    ps.setString(1, codPelicula);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); activos = rs.getInt(1); }
                }
                if (activos >= ejemplares) throw new SinEjemplaresException(titulo);
                int id;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO prestamo (codigo_pelicula, codigo_cliente, fecha_prestamo, estado) VALUES (?,?,?, 'A')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, codPelicula); ps.setString(2, codCliente); ps.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { k.next(); id = k.getInt(1); }
                }
                c.commit();
                return id;
            } catch (NegocioException | SQLException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    /** Devolucion: coloca fecha de devolucion y estado "I". */
    public void devolver(int codigo) throws NegocioException {
        try (Connection c = Database.conectar()) {
            String estado;
            try (PreparedStatement ps = c.prepareStatement("SELECT estado FROM prestamo WHERE codigo=?")) {
                ps.setInt(1, codigo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new NegocioException("No existe el prestamo " + codigo + ".");
                    estado = rs.getString(1);
                }
            }
            if (!"A".equals(estado)) throw new NegocioException("El prestamo " + codigo + " ya fue devuelto.");
            try (PreparedStatement ps = c.prepareStatement("UPDATE prestamo SET fecha_devolucion=?, estado='I' WHERE codigo=?")) {
                ps.setDate(1, java.sql.Date.valueOf(LocalDate.now())); ps.setInt(2, codigo);
                ps.executeUpdate();
            }
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    public Optional<Prestamo> buscar(int codigo) throws NegocioException {
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement("SELECT * FROM prestamo WHERE codigo=?")) {
            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    public List<Prestamo> listar(boolean soloActivos) throws NegocioException {
        String sql = "SELECT * FROM prestamo " + (soloActivos ? "WHERE estado='A' " : "") + "ORDER BY codigo DESC";
        try (Connection c = Database.conectar(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            List<Prestamo> r = new ArrayList<>();
            while (rs.next()) r.add(map(rs));
            return r;
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    private Prestamo map(ResultSet rs) throws SQLException {
        java.sql.Date d = rs.getDate("fecha_devolucion");
        return new Prestamo(rs.getInt("codigo"), rs.getString("codigo_pelicula"), rs.getString("codigo_cliente"),
            rs.getDate("fecha_prestamo").toLocalDate(), d == null ? null : d.toLocalDate(), rs.getString("estado"));
    }
}
