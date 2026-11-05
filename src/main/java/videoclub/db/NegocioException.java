package videoclub.db;

import java.sql.SQLException;

/** Error de validacion o de regla de negocio. Su mensaje se muestra tal cual al usuario. */
public class NegocioException extends Exception {

    public NegocioException(String mensaje) {
        super(mensaje);
    }

    public NegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /** Convierte un error de SQL en un error que la interfaz puede mostrar. */
    public static NegocioException deBaseDeDatos(SQLException causa) {
        return new NegocioException("Error de base de datos: " + causa.getMessage(), causa);
    }
}
