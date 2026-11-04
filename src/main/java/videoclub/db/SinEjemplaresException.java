package videoclub.db;

public class SinEjemplaresException extends NegocioException {
    public SinEjemplaresException(String titulo) {
        super("De la pelicula \"" + titulo + "\" ya no quedan ejemplares para prestar.");
    }
}
