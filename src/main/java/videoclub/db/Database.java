package videoclub.db;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Punto unico de acceso a la base de datos.
 * Orden de conexion: MySQL externo (si esta configurado), MariaDB embebida y, como respaldo, H2 en modo MySQL.
 */
public final class Database {
    private static final String CARPETA_DATOS = "data";
    private static final String ARCHIVO_CONFIG = "config.properties";
    private static final String ARCHIVO_ESQUEMA = "/schema.sql";
    private static final String NOMBRE_BD = "videoclub";

    private static String urlConexion;
    private static String usuario = "root";
    private static String clave = "";
    private static String motor = "";
    private static DB mariaDbEmbebida;

    private Database() { }

    /** Nombre del motor que quedo en uso, para mostrarlo en la interfaz. */
    public static String motor() {
        return motor;
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(urlConexion, usuario, clave);
    }

    /** Prueba los motores en orden hasta que uno funcione y crea el esquema. */
    public static void iniciar() throws Exception {
        Properties config = cargarConfiguracion();
        String modo = config.getProperty("modo", "auto").trim();
        String urlMysql = config.getProperty("mysql.url", "").trim();

        if (!modo.equals("embebida") && !urlMysql.isEmpty()) {
            try {
                conectarMysql(config, urlMysql);
                return;
            } catch (Exception e) {
                if (modo.equals("mysql")) {
                    throw new Exception("No se pudo conectar a MySQL: " + e.getMessage(), e);
                }
                // En modo auto se intenta la base embebida.
            }
        }

        try {
            iniciarMariaDb();
            motor = "MariaDB embebida (compatible con MySQL)";
            crearEsquema();
            return;
        } catch (Throwable e) {
            // Throwable porque el binario nativo de MariaDB puede fallar con un LinkageError.
            detenerMariaDb();
        }

        iniciarH2();
    }

    /** Detiene la base embebida de MariaDB si esta corriendo. */
    public static void detener() {
        detenerMariaDb();
    }

    private static Properties cargarConfiguracion() throws IOException {
        Properties config = new Properties();
        Path archivo = Paths.get(ARCHIVO_CONFIG);
        if (Files.exists(archivo)) {
            try (InputStream in = Files.newInputStream(archivo)) {
                config.load(in);
            }
        }
        return config;
    }

    private static void conectarMysql(Properties config, String url) throws Exception {
        urlConexion = url;
        usuario = config.getProperty("mysql.usuario", "root");
        clave = config.getProperty("mysql.clave", "");
        probar();
        motor = "MySQL externo";
        crearEsquema();
    }

    private static void iniciarMariaDb() throws Exception {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(0);
        config.setDataDir(rutaDatos("mariadb").toFile());
        config.setBaseDir(rutaDatos("mariadb-bin").toFile());

        mariaDbEmbebida = DB.newEmbeddedDB(config.build());
        mariaDbEmbebida.start();
        mariaDbEmbebida.createDB(NOMBRE_BD);

        urlConexion = config.getURL(NOMBRE_BD);
        usuario = "root";
        clave = "";
        probar();
    }

    private static void iniciarH2() throws Exception {
        try {
            Files.createDirectories(rutaDatos());
            Class.forName("org.h2.Driver");
            urlConexion = "jdbc:h2:file:" + rutaDatos("h2db").toAbsolutePath()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=FALSE";
            usuario = "sa";
            clave = "";
            probar();
            motor = "H2 embebida (modo MySQL)";
            crearEsquema();
        } catch (Exception e) {
            throw new Exception("No se pudo iniciar la base de datos: " + e.getMessage(), e);
        }
    }

    private static void detenerMariaDb() {
        if (mariaDbEmbebida != null) {
            try {
                mariaDbEmbebida.stop();
            } catch (Exception ignorada) {
                // Al cerrar la aplicacion no hay nada mas que hacer.
            }
            mariaDbEmbebida = null;
        }
    }

    private static Path rutaDatos(String... partes) {
        return Paths.get(CARPETA_DATOS, partes).toAbsolutePath();
    }

    private static void probar() throws SQLException {
        try (Connection c = conectar()) {
            if (!c.isValid(2)) {
                throw new SQLException("La conexion no respondio.");
            }
        }
    }

    /** Ejecuta las sentencias de schema.sql (las que ya existen no se vuelven a crear). */
    private static void crearEsquema() throws Exception {
        String sql;
        try (InputStream in = Database.class.getResourceAsStream(ARCHIVO_ESQUEMA)) {
            if (in == null) {
                throw new IOException("No se encontro " + ARCHIVO_ESQUEMA + " dentro del JAR.");
            }
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        String sinComentarios = sql.replaceAll("(?m)^--.*$", "");
        try (Connection c = conectar(); Statement st = c.createStatement()) {
            for (String sentencia : sinComentarios.split(";")) {
                if (!sentencia.isBlank()) {
                    st.execute(sentencia.trim());
                }
            }
        }
    }
}
