package videoclub.db;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/** Maneja la conexion: MySQL externo (si esta configurado) o H2 en modo MySQL como base embebida. */
public final class Database {
    private static String url, user = "root", pass = "";
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

        if (!modo.equals("embebida") && !mysql.isEmpty()) {
            try {
                url = mysql; user = p.getProperty("mysql.usuario", "root"); pass = p.getProperty("mysql.clave", "");
                probar(); motor = "MySQL externo"; crearEsquema(); return;
            } catch (Exception e) {
                if (modo.equals("mysql")) throw new Exception("No se pudo conectar a MySQL: " + e.getMessage(), e);
                // modo auto: caer a la base embebida
            }
        }
        try {
            Files.createDirectories(Paths.get("data"));
            Class.forName("org.h2.Driver");
            url = "jdbc:h2:file:" + Paths.get("data", "videoclub").toAbsolutePath()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=FALSE";
            user = "sa"; pass = "";
            probar(); motor = "H2 embebida (modo MySQL)"; crearEsquema();
        } catch (Exception e) {
            throw new Exception("No se pudo iniciar la base de datos: " + e.getMessage(), e);
        }
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
}
