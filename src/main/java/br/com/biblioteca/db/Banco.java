package br.com.biblioteca.db;
import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
public final class Banco {
    private final String url;
    public Banco(String url) { this.url = url; }
    public Connection conectar() throws SQLException { return DriverManager.getConnection(url, "sa", ""); }
    public void inicializar() throws SQLException, IOException {
        if (url.startsWith("jdbc:h2:./data/")) Files.createDirectories(Path.of("data"));
        try (InputStream input = Banco.class.getResourceAsStream("/schema.sql")) {
            if (input == null) throw new IOException("schema.sql não encontrado.");
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection c = conectar(); Statement s = c.createStatement()) {
                for (String comando : sql.split(";")) if (!comando.isBlank()) s.execute(comando);
            }
        }
    }
}

