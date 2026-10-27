package br.com.biblioteca.repository;
import br.com.biblioteca.db.Banco;
import br.com.biblioteca.model.Leitor;
import java.sql.*;
import java.util.*;
public final class LeitorRepositorio implements Repositorio<Leitor> {
    private final Banco banco;
    public LeitorRepositorio(Banco banco) { this.banco=banco; }
    private Leitor mapear(ResultSet r) throws SQLException { return new Leitor(r.getLong("id"),r.getString("nome"),r.getString("email")); }
    public long salvar(Leitor l) throws SQLException {
        try(var c=banco.conectar(); var s=c.prepareStatement("INSERT INTO leitores(nome,email) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1,l.getNome()); s.setString(2,l.getEmail()); s.executeUpdate();
            try(var r=s.getGeneratedKeys()) { if(!r.next()) throw new SQLException("ID não gerado."); return r.getLong(1); }
        }
    }
    public Optional<Leitor> buscar(long id) throws SQLException {
        try(var c=banco.conectar(); var s=c.prepareStatement("SELECT * FROM leitores WHERE id=?")) {
            s.setLong(1,id); try(var r=s.executeQuery()) { return r.next()?Optional.of(mapear(r)):Optional.empty(); }
        }
    }
    public List<Leitor> listar() throws SQLException {
        var lista=new ArrayList<Leitor>();
        try(var c=banco.conectar(); var s=c.prepareStatement("SELECT * FROM leitores ORDER BY nome"); var r=s.executeQuery()) {
            while(r.next()) lista.add(mapear(r));
        } return lista;
    }
    public boolean excluir(long id) throws SQLException {
        try(var c=banco.conectar(); var s=c.prepareStatement("DELETE FROM leitores WHERE id=?")) {
            s.setLong(1,id); return s.executeUpdate()==1;
        }
    }
    public boolean atualizar(long id,String nome,String email) throws SQLException {
        try(var c=banco.conectar(); var s=c.prepareStatement("UPDATE leitores SET nome=?, email=? WHERE id=?")) {
            s.setString(1,nome); s.setString(2,email); s.setLong(3,id); return s.executeUpdate()==1;
        }
    }
}

