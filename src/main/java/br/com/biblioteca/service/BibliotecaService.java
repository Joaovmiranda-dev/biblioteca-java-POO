package br.com.biblioteca.service;
import br.com.biblioteca.db.Banco;
import br.com.biblioteca.model.*;
import br.com.biblioteca.repository.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
public final class BibliotecaService {
    private final Banco banco;
    private final LivroRepositorio livros;
    private final LeitorRepositorio leitores;
    public BibliotecaService(Banco banco) {
        this.banco=banco; livros=new LivroRepositorio(banco); leitores=new LeitorRepositorio(banco);
    }
    private String texto(String valor,int limite,String campo) {
        if(valor==null || valor.isBlank() || valor.strip().length()>limite)
            throw new RegraNegocioException(campo+" deve ter entre 1 e "+limite+" caracteres.");
        return valor.strip();
    }
    private String email(String valor) {
        String e=texto(valor,200,"E-mail").toLowerCase(Locale.ROOT);
        if(!e.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new RegraNegocioException("E-mail inválido.");
        return e;
    }
    public long cadastrarLivro(String titulo,String autor,String isbn,int total) throws SQLException {
        if(total<1) throw new RegraNegocioException("Quantidade deve ser positiva.");
        return livros.salvar(new Livro(0,texto(titulo,200,"Título"),texto(autor,150,"Autor"),texto(isbn,30,"ISBN"),total,total));
    }
    public long cadastrarLeitor(String nome,String email) throws SQLException {
        return leitores.salvar(new Leitor(0,texto(nome,150,"Nome"),email(email)));
    }
    public List<Livro> livros() throws SQLException { return livros.listar(); }
    public List<Leitor> leitores() throws SQLException { return leitores.listar(); }
    public void atualizarLivro(long id,String titulo,String autor) throws SQLException {
        if(!livros.atualizar(id,texto(titulo,200,"Título"),texto(autor,150,"Autor"))) throw new RegraNegocioException("Livro não encontrado.");
    }
    public void atualizarLeitor(long id,String nome,String email) throws SQLException {
        if(!leitores.atualizar(id,texto(nome,150,"Nome"),email(email))) throw new RegraNegocioException("Leitor não encontrado.");
    }
    public void excluirLivro(long id) throws SQLException {
        if(!livros.excluir(id)) throw new RegraNegocioException("Livro não encontrado.");
    }
    public void excluirLeitor(long id) throws SQLException {
        if(!leitores.excluir(id)) throw new RegraNegocioException("Leitor não encontrado.");
    }
    public long emprestar(long livroId,long leitorId,int dias) throws SQLException {
        if(dias<1 || dias>60) throw new RegraNegocioException("Prazo deve ser entre 1 e 60 dias.");
        try(var c=banco.conectar()) {
            c.setAutoCommit(false);
            try {
                try(var s=c.prepareStatement("SELECT id FROM leitores WHERE id=?")) {
                    s.setLong(1,leitorId); try(var r=s.executeQuery()) { if(!r.next()) throw new RegraNegocioException("Leitor não encontrado."); }
                }
                // A atualização condicional bloqueia a linha e impede estoque negativo em empréstimos simultâneos.
                try(var s=c.prepareStatement("UPDATE livros SET disponiveis=disponiveis-1 WHERE id=? AND disponiveis>0")) {
                    s.setLong(1,livroId); if(s.executeUpdate()!=1) throw new RegraNegocioException("Livro inexistente ou sem exemplares disponíveis.");
                }
                long id;
                try(var s=c.prepareStatement("INSERT INTO emprestimos(livro_id,leitor_id,realizado_em,previsto_em) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    LocalDate hoje=LocalDate.now();
                    s.setLong(1,livroId); s.setLong(2,leitorId); s.setObject(3,hoje); s.setObject(4,hoje.plusDays(dias)); s.executeUpdate();
                    try(var r=s.getGeneratedKeys()) { if(!r.next()) throw new SQLException("ID não gerado."); id=r.getLong(1); }
                }
                c.commit(); return id;
            } catch(SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    public void devolver(long id) throws SQLException {
        try(var c=banco.conectar()) {
            c.setAutoCommit(false);
            try {
                long livroId;
                try(var s=c.prepareStatement("SELECT livro_id,devolvido_em FROM emprestimos WHERE id=? FOR UPDATE")) {
                    s.setLong(1,id);
                    try(var r=s.executeQuery()) {
                        if(!r.next()) throw new RegraNegocioException("Empréstimo não encontrado.");
                        if(r.getDate("devolvido_em")!=null) throw new RegraNegocioException("Empréstimo já devolvido.");
                        livroId=r.getLong("livro_id");
                    }
                }
                try(var s=c.prepareStatement("UPDATE emprestimos SET devolvido_em=? WHERE id=?")) {
                    s.setObject(1,LocalDate.now()); s.setLong(2,id); s.executeUpdate();
                }
                try(var s=c.prepareStatement("UPDATE livros SET disponiveis=disponiveis+1 WHERE id=?")) {
                    s.setLong(1,livroId); s.executeUpdate();
                }
                c.commit();
            } catch(SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    public List<Emprestimo> emprestimos(boolean somenteAbertos) throws SQLException {
        var lista=new ArrayList<Emprestimo>();
        String sql="SELECT e.*,l.titulo,u.nome FROM emprestimos e JOIN livros l ON l.id=e.livro_id JOIN leitores u ON u.id=e.leitor_id"
            +(somenteAbertos?" WHERE e.devolvido_em IS NULL":"")+" ORDER BY e.id";
        try(var c=banco.conectar(); var s=c.prepareStatement(sql); var r=s.executeQuery()) {
            while(r.next()) lista.add(new Emprestimo(r.getLong("id"),r.getString("titulo"),r.getString("nome"),
                r.getObject("realizado_em",LocalDate.class),r.getObject("previsto_em",LocalDate.class),r.getObject("devolvido_em",LocalDate.class)));
        } return lista;
    }
}

