package br.com.biblioteca;
import br.com.biblioteca.db.Banco;
import br.com.biblioteca.service.*;
import java.sql.SQLException;
import java.util.concurrent.*;
public final class IntegrationTest {
    private static int verificacoes;
    private static void verificar(boolean ok,String mensagem) {
        if(!ok) throw new AssertionError(mensagem); verificacoes++;
    }
    @FunctionalInterface interface Acao { void executar() throws Exception; }
    private static void rejeitar(Class<? extends Exception> tipo,Acao acao) throws Exception {
        try { acao.executar(); } catch(Exception e) {
            if(!tipo.isInstance(e)) throw e; verificacoes++; return;
        } throw new AssertionError("Operação deveria falhar: "+tipo.getSimpleName());
    }
    public static void main(String[] args) throws Exception {
        Banco banco=new Banco("jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1");
        banco.inicializar(); banco.inicializar();
        var s=new BibliotecaService(banco);
        rejeitar(RegraNegocioException.class,()->s.cadastrarLivro(" ","Autor","1",1));
        rejeitar(RegraNegocioException.class,()->s.cadastrarLivro("Livro","Autor","1",0));
        rejeitar(RegraNegocioException.class,()->s.cadastrarLeitor("Ana","invalido"));
        long l=s.cadastrarLivro("Dom Casmurro","Machado de Assis","9788535914849",1);
        long u=s.cadastrarLeitor("Ana","ANA@EXEMPLO.COM");
        rejeitar(SQLException.class,()->s.cadastrarLivro("Outro","Autor","9788535914849",1));
        rejeitar(SQLException.class,()->s.cadastrarLeitor("Outra","ana@exemplo.com"));
        s.atualizarLivro(l,"Dom Casmurro (edição revisada)","Machado de Assis");
        s.atualizarLeitor(u,"Ana Silva","ana@exemplo.com");
        verificar(s.leitores().get(0).getNome().equals("Ana Silva"),"Atualização de leitor");
        rejeitar(RegraNegocioException.class,()->s.emprestar(l,u,0));
        rejeitar(RegraNegocioException.class,()->s.emprestar(l,999,7));
        verificar(s.livros().get(0).getDisponiveis()==1,"Falha não consome estoque");
        long e=s.emprestar(l,u,7);
        verificar(s.livros().get(0).getDisponiveis()==0,"Baixa de estoque");
        rejeitar(RegraNegocioException.class,()->s.emprestar(l,u,7));
        rejeitar(SQLException.class,()->s.excluirLivro(l));
        rejeitar(SQLException.class,()->s.excluirLeitor(u));
        verificar(s.emprestimos(true).size()==1,"Listagem de abertos");
        s.devolver(e);
        rejeitar(RegraNegocioException.class,()->s.devolver(e));
        verificar(s.livros().get(0).getDisponiveis()==1,"Devolução única");
        verificar(s.emprestimos(true).isEmpty(),"Empréstimo encerrado");
        verificar(s.emprestimos(false).size()==1,"Histórico preservado");
        long removivel=s.cadastrarLivro("Temporário","Autor","remover",1);
        s.excluirLivro(removivel);
        verificar(s.livros().size()==1,"Exclusão sem histórico");
        long leitorRemovivel=s.cadastrarLeitor("Temporário","temp@exemplo.com");
        s.excluirLeitor(leitorRemovivel);
        verificar(s.leitores().size()==1,"Exclusão de leitor");
        // Duas solicitações simultâneas para o último exemplar: somente uma pode vencer.
        ExecutorService pool=Executors.newFixedThreadPool(2);
        CountDownLatch inicio=new CountDownLatch(1);
        Callable<Boolean> tarefa=()->{
            inicio.await();
            try { s.emprestar(l,u,7); return true; }
            catch(RegraNegocioException erro) { return false; }
        };
        try {
            Future<Boolean> a=pool.submit(tarefa), b=pool.submit(tarefa); inicio.countDown();
            verificar(a.get(10,TimeUnit.SECONDS)^b.get(10,TimeUnit.SECONDS),"Concorrência deve permitir apenas um empréstimo");
            verificar(s.livros().get(0).getDisponiveis()==0,"Estoque nunca negativo");
        } finally { pool.shutdownNow(); }
        // Um novo serviço consultando o mesmo banco deve enxergar os registros.
        verificar(new BibliotecaService(banco).emprestimos(false).size()==2,"Dados compartilhados entre conexões");
        System.out.println("OK: "+verificacoes+" verificações de integração.");
    }
}

