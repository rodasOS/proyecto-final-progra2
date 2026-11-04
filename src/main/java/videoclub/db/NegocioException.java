package videoclub.db;

/** Error de regla de negocio o validacion (se muestra al usuario tal cual). */
public class NegocioException extends Exception {
    public NegocioException(String msg) { super(msg); }
    public NegocioException(String msg, Throwable causa) { super(msg, causa); }
}
