package videoclub.dao;

import videoclub.db.Database;
import videoclub.db.NegocioException;
import videoclub.db.SinEjemplaresException;
import videoclub.model.Estado;
import videoclub.model.Prestamo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a la tabla de prestamos y reglas de prestar y devolver. */
public class PrestamoDao {
    private static final String SQL_BLOQUEAR_PELICULA =
        "SELECT titulo, ejemplares FROM pelicula WHERE codigo = ? FOR UPDATE";
    private static final String SQL_ESTADO_CLIENTE = "SELECT estado FROM cliente WHERE codigo = ?";
    private static final String SQL_CONTAR_ACTIVOS =
        "SELECT COUNT(*) FROM prestamo WHERE codigo_pelicula = ? AND estado = ?";
    private static final String SQL_INSERTAR =
        "INSERT INTO prestamo (codigo_pelicula, codigo_cliente, fecha_prestamo, estado) VALUES (?, ?, ?, ?)";
    private static final String SQL_DEVOLVER =
        "UPDATE prestamo SET fecha_devolucion = ?, estado = ? WHERE codigo = ? AND estado = ?";
    private static final String SQL_BUSCAR = "SELECT * FROM prestamo WHERE codigo = ?";

    /**
     * Registra un prestamo activo y devuelve su codigo.
     * Lanza SinEjemplaresException si la pelicula ya no tiene copias disponibles.
     * Todo ocurre en una transaccion; la fila de la pelicula queda bloqueada hasta el commit.
     */
    public int prestar(String codigoPelicula, String codigoCliente) throws NegocioException {
        try (Connection c = Database.conectar()) {
            c.setAutoCommit(false);
            try {
                Copias copias = bloquearPelicula(c, codigoPelicula);
                verificarClienteActivo(c, codigoCliente);
                if (contarActivos(c, codigoPelicula) >= copias.ejemplares()) {
                    throw new SinEjemplaresException(copias.titulo());
                }
                int codigoPrestamo = insertarPrestamo(c, codigoPelicula, codigoCliente);
                c.commit();
                return codigoPrestamo;
            } catch (NegocioException | SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw NegocioException.deBaseDeDatos(e);
        }
    }

    /** Registra la devolucion: fecha de hoy y estado inactivo. Solo afecta prestamos activos. */
    public void devolver(int codigoPrestamo) throws NegocioException {
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(SQL_DEVOLVER)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setString(2, Estado.INACTIVO);
            ps.setInt(3, codigoPrestamo);
            ps.setString(4, Estado.ACTIVO);
            if (ps.executeUpdate() == 0) {
                throw buscar(codigoPrestamo).isEmpty()
                    ? new NegocioException("No existe el préstamo " + codigoPrestamo + ".")
                    : new NegocioException("El préstamo " + codigoPrestamo + " ya fue devuelto.");
            }
        } catch (SQLException e) {
            throw NegocioException.deBaseDeDatos(e);
        }
    }

    public Optional<Prestamo> buscar(int codigoPrestamo) throws NegocioException {
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, codigoPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw NegocioException.deBaseDeDatos(e);
        }
    }

    /** Lista los prestamos, del mas reciente al mas antiguo. */
    public List<Prestamo> listar(boolean soloActivos) throws NegocioException {
        String sql = "SELECT * FROM prestamo " + (soloActivos ? "WHERE estado = ? " : "") + "ORDER BY codigo DESC";
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (soloActivos) {
                ps.setString(1, Estado.ACTIVO);
            }
            List<Prestamo> prestamos = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapear(rs));
                }
            }
            return prestamos;
        } catch (SQLException e) {
            throw NegocioException.deBaseDeDatos(e);
        }
    }

    /** Titulo y numero de copias de una pelicula, leidos con bloqueo de fila. */
    private record Copias(String titulo, int ejemplares) { }

    private static Copias bloquearPelicula(Connection c, String codigoPelicula)
            throws SQLException, NegocioException {
        try (PreparedStatement ps = c.prepareStatement(SQL_BLOQUEAR_PELICULA)) {
            ps.setString(1, codigoPelicula);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new NegocioException("La película " + codigoPelicula + " no existe.");
                }
                return new Copias(rs.getString("titulo"), rs.getInt("ejemplares"));
            }
        }
    }

    private static void verificarClienteActivo(Connection c, String codigoCliente)
            throws SQLException, NegocioException {
        try (PreparedStatement ps = c.prepareStatement(SQL_ESTADO_CLIENTE)) {
            ps.setString(1, codigoCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new NegocioException("El cliente " + codigoCliente + " no existe.");
                }
                if (!Estado.ACTIVO.equals(rs.getString("estado"))) {
                    throw new NegocioException("El cliente está inactivo y no puede recibir préstamos.");
                }
            }
        }
    }

    private static int contarActivos(Connection c, String codigoPelicula) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_CONTAR_ACTIVOS)) {
            ps.setString(1, codigoPelicula);
            ps.setString(2, Estado.ACTIVO);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static int insertarPrestamo(Connection c, String codigoPelicula, String codigoCliente)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, codigoPelicula);
            ps.setString(2, codigoCliente);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setString(4, Estado.ACTIVO);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    private static Prestamo mapear(ResultSet rs) throws SQLException {
        Date devolucion = rs.getDate("fecha_devolucion");
        return new Prestamo(
            rs.getInt("codigo"),
            rs.getString("codigo_pelicula"),
            rs.getString("codigo_cliente"),
            rs.getDate("fecha_prestamo").toLocalDate(),
            devolucion == null ? null : devolucion.toLocalDate(),
            rs.getString("estado"));
    }
}
