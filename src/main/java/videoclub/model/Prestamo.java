package videoclub.model;

import java.time.LocalDate;

public record Prestamo(int codigo, String codigoPelicula, String codigoCliente,
                       LocalDate fechaPrestamo, LocalDate fechaDevolucion, String estado) { }
