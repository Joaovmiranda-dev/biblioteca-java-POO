package br.com.biblioteca.model;
public final class Livro extends Entidade {
    private final String titulo, autor, isbn;
    private final int total, disponiveis;
    public Livro(long id, String titulo, String autor, String isbn, int total, int disponiveis) {
        super(id); this.titulo = titulo; this.autor = autor; this.isbn = isbn;
        this.total = total; this.disponiveis = disponiveis;
    }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getIsbn() { return isbn; }
    public int getTotal() { return total; }
    public int getDisponiveis() { return disponiveis; }
    @Override public String resumo() {
        return "%d | %s | %s | ISBN %s | disponíveis %d/%d".formatted(getId(), titulo, autor, isbn, disponiveis, total);
    }
}

