package videoclub.dao;

import videoclub.db.Database;
import videoclub.db.NegocioException;
import videoclub.model.Cliente;

import java.sql.*;
import java.util.*;

public class ClienteDao {
    public void insertar(Cliente c) throws NegocioException {
        validar(c);
        try (Connection cn = Database.conectar()) {
            try (PreparedStatement q = cn.prepareStatement("SELECT 1 FROM cliente WHERE codigo=?")) {
                q.setString(1, c.codigo());
                try (ResultSet rs = q.executeQuery()) { if (rs.next()) throw new NegocioException("Ya existe un cliente con codigo " + c.codigo()); }
            }
            try (PreparedStatement ps = cn.prepareStatement("INSERT INTO cliente VALUES (?,?,?,?,?,?)")) {
                ps.setString(1, c.codigo()); ps.setString(2, c.nombre()); ps.setString(3, c.direccion());
                ps.setString(4, c.telefono()); ps.setString(5, c.email()); ps.setString(6, c.estado());
                ps.executeUpdate();
            }
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    public void actualizar(Cliente c) throws NegocioException {
        validar(c);
        String sql = "UPDATE cliente SET nombre=?,direccion=?,telefono=?,email=?,estado=? WHERE codigo=?";
        try (Connection cn = Database.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.nombre()); ps.setString(2, c.direccion()); ps.setString(3, c.telefono());
            ps.setString(4, c.email()); ps.setString(5, c.estado()); ps.setString(6, c.codigo());
            if (ps.executeUpdate() == 0) throw new NegocioException("No existe el cliente " + c.codigo());
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    public Optional<Cliente> buscar(String codigo) throws NegocioException {
        try (Connection cn = Database.conectar(); PreparedStatement ps = cn.prepareStatement("SELECT * FROM cliente WHERE codigo=?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    public List<Cliente> listar(String filtro) throws NegocioException {
        String sql = "SELECT * FROM cliente WHERE codigo LIKE ? OR nombre LIKE ? OR email LIKE ? ORDER BY nombre";
        try (Connection cn = Database.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            String f = "%" + (filtro == null ? "" : filtro.trim()) + "%";
            for (int i = 1; i <= 3; i++) ps.setString(i, f);
            List<Cliente> r = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) r.add(map(rs)); }
            return r;
        } catch (SQLException e) { throw new NegocioException("Error de base de datos: " + e.getMessage(), e); }
    }

    private void validar(Cliente c) throws NegocioException {
        if (c.codigo().isBlank() || c.nombre().isBlank()) throw new NegocioException("Codigo y nombre son obligatorios.");
        if (!c.email().isBlank() && !c.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new NegocioException("El email no es valido.");
        if (!c.telefono().isBlank() && !c.telefono().matches("^[0-9+\\-\\s()]{6,20}$")) throw new NegocioException("El telefono no es valido.");
    }

    private Cliente map(ResultSet rs) throws SQLException {
        return new Cliente(rs.getString("codigo"), rs.getString("nombre"), rs.getString("direccion"),
            rs.getString("telefono"), rs.getString("email"), rs.getString("estado"));
    }
}
