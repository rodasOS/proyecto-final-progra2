package videoclub.model;

public record Pelicula(String codigo, String titulo, int anio, int duracion, String clasificacion,
                       String actores, String director, int ejemplares) { }
