-- Esquema compatible con MySQL / MariaDB / H2 (MODE=MySQL)
CREATE TABLE IF NOT EXISTS pelicula (
  codigo      VARCHAR(20)  NOT NULL PRIMARY KEY,
  titulo      VARCHAR(150) NOT NULL,
  anio        INT          NOT NULL,
  duracion    INT          NOT NULL,
  clasificacion VARCHAR(20) NOT NULL,
  actores     VARCHAR(500),
  director    VARCHAR(100),
  ejemplares  INT          NOT NULL
);
CREATE TABLE IF NOT EXISTS cliente (
  codigo    VARCHAR(20)  NOT NULL PRIMARY KEY,
  nombre    VARCHAR(100) NOT NULL,
  direccion VARCHAR(200),
  telefono  VARCHAR(30),
  email     VARCHAR(100),
  estado    CHAR(1) NOT NULL DEFAULT 'A'
);
CREATE TABLE IF NOT EXISTS prestamo (
  codigo           INT AUTO_INCREMENT PRIMARY KEY,
  codigo_pelicula  VARCHAR(20) NOT NULL,
  codigo_cliente   VARCHAR(20) NOT NULL,
  fecha_prestamo   DATE NOT NULL,
  fecha_devolucion DATE NULL,
  estado           CHAR(1) NOT NULL DEFAULT 'A',
  FOREIGN KEY (codigo_pelicula) REFERENCES pelicula(codigo),
  FOREIGN KEY (codigo_cliente)  REFERENCES cliente(codigo)
);
