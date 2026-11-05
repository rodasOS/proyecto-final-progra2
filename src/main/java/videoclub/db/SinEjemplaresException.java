package videoclub.db;

/** Se lanza al prestar una pelicula cuyas copias ya estan todas prestadas. */
public class SinEjemplaresException extends NegocioException {

    public SinEjemplaresException(String titulo) {
        super("De la película \"" + titulo + "\" ya no quedan ejemplares para prestar.");
    }
}
