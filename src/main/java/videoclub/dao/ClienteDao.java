package videoclub.dao;

import videoclub.db.Database;
import videoclub.db.NegocioException;
import videoclub.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a la tabla de clientes. */
public class ClienteDao {
    private static final String REGEX_EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private static final String REGEX_TELEFONO = "^[0-9+\\-\\s()]{6,20}$";

    private static final String SQL_INSERTAR =
        "INSERT INTO cliente (codigo, nombre, direccion, telefono, email, estado) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
        "UPDATE cliente SET nombre = ?, direccion = ?, telefono = ?, email = ?, estado = ? WHERE codigo = ?";
    private static final String SQL_BUSCAR = "SELECT * FROM cliente WHERE codigo = ?";
    private static final String SQL_LISTAR =
        "SELECT * FROM cliente WHERE codigo LIKE ? OR nombre LIKE ? OR email LIKE ? ORDER BY nombre";
    private static final String SQL_EXISTE = "SELECT 1 FROM cliente WHERE codigo = ?";

    public void insertar(Cliente cliente) throws NegocioException {
        validar(cliente);
        try (Connection c = Database.conectar()) {
            if (existe(c, cliente.codigo())) {
                throw new NegocioException("Ya existe un cliente con código " + cliente.codigo() + ".");
            }
            try (PreparedStatement ps = c.prepareStatement(SQL_INSERTAR)) {
                ps.setString(1, cliente.codigo());
                ps.setString(2, cliente.nombre());
                ps.setString(3, cliente.direccion());
                ps.setString(4, cliente.telefono());
                ps.setString(5, cliente.email());
                ps.setString(6, cliente.estado());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw NegocioException.deBaseDeDatos(e);
        }
    }

    public void actualizar(Cliente cliente) throws NegocioException {
        validar(cliente);
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, cliente.nombre());
            ps.setString(2, cliente.direccion());
            ps.setString(3, cliente.telefono());
            ps.setString(4, cliente.email());
            ps.setString(5, cliente.estado());
            ps.setString(6, cliente.codigo());
            if (ps.executeUpdate() == 0) {
                throw new NegocioException("No existe el cliente " + cliente.codigo() + ".");
            }
        } catch (SQLException e) {
            throw NegocioException.deBaseDeDatos(e);
        }
    }

    public Optional<Cliente> buscar(String codigo) throws NegocioException {
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(SQL_BUSCAR)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw NegocioException.deBaseDeDatos(e);
        }
    }

    /** Busca por codigo, nombre o email. Un filtro vacio devuelve todos. */
    public List<Cliente> listar(String filtro) throws NegocioException {
        String patron = "%" + (filtro == null ? "" : filtro.trim()) + "%";
        try (Connection c = Database.conectar(); PreparedStatement ps = c.prepareStatement(SQL_LISTAR)) {
            for (int i = 1; i <= 3; i++) {
                ps.setString(i, patron);
            }
            List<Cliente> clientes = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapear(rs));
                }
            }
            return clientes;
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

    private static void validar(Cliente cliente) throws NegocioException {
        if (cliente.codigo().isBlank() || cliente.nombre().isBlank()) {
            throw new NegocioException("Código y nombre son obligatorios.");
        }
        if (!cliente.email().isBlank() && !cliente.email().matches(REGEX_EMAIL)) {
            throw new NegocioException("El email no es válido.");
        }
        if (!cliente.telefono().isBlank() && !cliente.telefono().matches(REGEX_TELEFONO)) {
            throw new NegocioException("El teléfono no es válido.");
        }
    }

    private static Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getString("direccion"),
            rs.getString("telefono"),
            rs.getString("email"),
            rs.getString("estado"));
    }
}
