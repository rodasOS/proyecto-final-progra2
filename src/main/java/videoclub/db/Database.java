package videoclub.db;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/** Maneja la conexion: MySQL externo -> MariaDB embebida -> H2 (modo MySQL). */
public final class Database {
    private static String url, user = "root", pass = "";
    private static DB embebida;
    private static String motor = "";

    private Database() { }

    public static String motor() { return motor; }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }

    public static void iniciar() throws Exception {
        Properties p = new Properties();
        Path cfg = Paths.get("config.properties");
        if (Files.exists(cfg)) try (InputStream in = Files.newInputStream(cfg)) { p.load(in); }
        String modo = p.getProperty("modo", "auto").trim();
        String mysql = p.getProperty("mysql.url", "").trim();
        Exception ultimo = null;

        if (!modo.equals("embebida") && !mysql.isEmpty()) {
            try {
                url = mysql; user = p.getProperty("mysql.usuario", "root"); pass = p.getProperty("mysql.clave", "");
                probar(); motor = "MySQL externo"; crearEsquema(); return;
            } catch (Exception e) {
                ultimo = e;
                if (modo.equals("mysql")) throw new Exception("No se pudo conectar a MySQL: " + e.getMessage(), e);
            }
        }
        try {
            iniciarMariaDb(); motor = "MariaDB embebida (compatible MySQL)"; crearEsquema(); return;
        } catch (Throwable e) {
            if (embebida != null) { try { embebida.stop(); } catch (Exception ignored) { } embebida = null; }
            ultimo = e instanceof Exception ex ? ex : new Exception(e);
        }
        try {
            Files.createDirectories(Paths.get("data"));
            Class.forName("org.h2.Driver");
            url = "jdbc:h2:file:" + Paths.get("data", "h2db").toAbsolutePath()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=FALSE";
            user = "sa"; pass = "";
            probar(); motor = "H2 embebida (modo MySQL)"; crearEsquema();
        } catch (Exception e) {
            throw new Exception("Fallaron todos los motores de BD. Ultimo error: " + e.getMessage(), ultimo != null ? ultimo : e);
        }
    }

    private static void iniciarMariaDb() throws Exception {
        DBConfigurationBuilder cfg = DBConfigurationBuilder.newBuilder();
        cfg.setPort(0);
        cfg.setDataDir(Paths.get("data", "mariadb").toAbsolutePath().toFile());
        cfg.setBaseDir(Paths.get("data", "mariadb-bin").toAbsolutePath().toFile());
        embebida = DB.newEmbeddedDB(cfg.build());
        embebida.start();
        embebida.createDB("videoclub");
        url = cfg.getURL("videoclub");
        user = "root"; pass = "";
        probar();
    }

    private static void probar() throws SQLException { try (Connection c = conectar()) { c.isValid(2); } }

    private static void crearEsquema() throws Exception {
        String sql;
        try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            if (in == null) throw new FileNotFoundException("schema.sql no encontrado en el JAR");
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Connection c = conectar(); Statement st = c.createStatement()) {
            for (String s : sql.replaceAll("(?m)^--.*$", "").split(";"))
                if (!s.isBlank()) st.execute(s.trim());
        }
    }

    public static void detener() {
        if (embebida != null) { try { embebida.stop(); } catch (Exception ignored) { } }
    }
}
