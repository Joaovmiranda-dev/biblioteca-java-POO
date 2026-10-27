package br.com.biblioteca;
import br.com.biblioteca.db.Banco;
import br.com.biblioteca.service.*;
import java.sql.SQLException;
import java.util.*;
import java.nio.charset.StandardCharsets;
public final class Main {
    private final Scanner entrada=new Scanner(System.in,StandardCharsets.UTF_8);
    private final BibliotecaService service;
    private Main(BibliotecaService service) { this.service=service; }
    public static void main(String[] args) {
        try {
            Banco banco=new Banco(System.getenv().getOrDefault("BIBLIOTECA_DB_URL","jdbc:h2:./data/biblioteca"));
            banco.inicializar();
            new Main(new BibliotecaService(banco)).executar();
        } catch(Exception e) {
            System.err.println("Não foi possível iniciar a biblioteca: "+e.getMessage()); System.exit(1);
        }
    }
    private String ler(String pergunta) {
        System.out.print(pergunta+": ");
        if(!entrada.hasNextLine()) throw new NoSuchElementException();
        return entrada.nextLine();
    }
    private long id(String pergunta) { return Long.parseLong(ler(pergunta)); }
    private int numero(String pergunta) { return Integer.parseInt(ler(pergunta)); }
    private void listar(List<String> linhas) {
        if(linhas.isEmpty()) System.out.println("Nenhum registro encontrado.");
        else linhas.forEach(System.out::println);
    }
    private void executar() {
        while(true) {
            System.out.println("""
                    
                    === BIBLIOTECA ===
                    1 Cadastrar livro       2 Listar livros
                    3 Atualizar livro       4 Excluir livro
                    5 Cadastrar leitor      6 Listar leitores
                    7 Atualizar leitor      8 Excluir leitor
                    9 Emprestar livro      10 Devolver livro
                    11 Histórico           12 Empréstimos abertos
                    0 Sair
                    """);
            try {
                switch(numero("Opção")) {
                    case 0 -> { System.out.println("Até logo!"); return; }
                    case 1 -> System.out.println("Livro cadastrado: "+service.cadastrarLivro(ler("Título"),ler("Autor"),ler("ISBN"),numero("Exemplares")));
                    case 2 -> listar(service.livros().stream().map(l->l.resumo()).toList());
                    case 3 -> { service.atualizarLivro(id("ID do livro"),ler("Novo título"),ler("Novo autor")); System.out.println("Livro atualizado."); }
                    case 4 -> { service.excluirLivro(id("ID do livro")); System.out.println("Livro excluído."); }
                    case 5 -> System.out.println("Leitor cadastrado: "+service.cadastrarLeitor(ler("Nome"),ler("E-mail")));
                    case 6 -> listar(service.leitores().stream().map(l->l.resumo()).toList());
                    case 7 -> { service.atualizarLeitor(id("ID do leitor"),ler("Novo nome"),ler("Novo e-mail")); System.out.println("Leitor atualizado."); }
                    case 8 -> { service.excluirLeitor(id("ID do leitor")); System.out.println("Leitor excluído."); }
                    case 9 -> System.out.println("Empréstimo registrado: "+service.emprestar(id("ID do livro"),id("ID do leitor"),numero("Prazo em dias (1–60)")));
                    case 10 -> { service.devolver(id("ID do empréstimo")); System.out.println("Devolução registrada."); }
                    case 11 -> listar(service.emprestimos(false).stream().map(e->e.resumo()).toList());
                    case 12 -> listar(service.emprestimos(true).stream().map(e->e.resumo()).toList());
                    default -> System.out.println("Opção inválida.");
                }
            } catch(NumberFormatException e) { System.out.println("Informe um número inteiro válido."); }
              catch(RegraNegocioException e) { System.out.println(e.getMessage()); }
              catch(SQLException e) {
                String estado=e.getSQLState();
                if("23505".equals(estado)) System.out.println("ISBN ou e-mail já cadastrado.");
                else if("23503".equals(estado)) System.out.println("Registro possui histórico de empréstimos e não pode ser excluído.");
                else System.out.println("Erro ao acessar o banco. Código: "+estado);
            } catch(NoSuchElementException e) { System.out.println("Entrada encerrada."); return; }
        }
    }
}

