package br.com.biblioteca.repository;
import br.com.biblioteca.db.Banco;
import br.com.biblioteca.model.Livro;
import java.sql.*;
import java.util.*;
public final class LivroRepositorio implements Repositorio<Livro> {
    private final Banco banco;
    public LivroRepositorio(Banco banco) { this.banco = banco; }
    private Livro mapear(ResultSet r) throws SQLException {
        return new Livro(r.getLong("id"), r.getString("titulo"), r.getString("autor"),
            r.getString("isbn"), r.getInt("total"), r.getInt("disponiveis"));
    }
    public long salvar(Livro l) throws SQLException {
        try (var c = banco.conectar(); var s = c.prepareStatement(
                "INSERT INTO livros(titulo,autor,isbn,total,disponiveis) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1,l.getTitulo()); s.setString(2,l.getAutor()); s.setString(3,l.getIsbn());
            s.setInt(4,l.getTotal()); s.setInt(5,l.getDisponiveis()); s.executeUpdate();
            try (var r=s.getGeneratedKeys()) { if (!r.next()) throw new SQLException("ID não gerado."); return r.getLong(1); }
        }
    }
    public Optional<Livro> buscar(long id) throws SQLException {
        try (var c=banco.conectar(); var s=c.prepareStatement("SELECT * FROM livros WHERE id=?")) {
            s.setLong(1,id); try(var r=s.executeQuery()) { return r.next()?Optional.of(mapear(r)):Optional.empty(); }
        }
    }
    public List<Livro> listar() throws SQLException {
        var lista=new ArrayList<Livro>();
        try(var c=banco.conectar(); var s=c.prepareStatement("SELECT * FROM livros ORDER BY titulo"); var r=s.executeQuery()) {
            while(r.next()) lista.add(mapear(r));
        } return lista;
    }
    public boolean excluir(long id) throws SQLException {
        try(var c=banco.conectar(); var s=c.prepareStatement("DELETE FROM livros WHERE id=?")) {
            s.setLong(1,id); return s.executeUpdate()==1;
        }
    }
    public boolean atualizar(long id, String titulo, String autor) throws SQLException {
        try(var c=banco.conectar(); var s=c.prepareStatement("UPDATE livros SET titulo=?, autor=? WHERE id=?")) {
            s.setString(1,titulo); s.setString(2,autor); s.setLong(3,id); return s.executeUpdate()==1;
        }
    }
}

