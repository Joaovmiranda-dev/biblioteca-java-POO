package br.com.biblioteca.model;
public final class Leitor extends Entidade {
    private final String nome, email;
    public Leitor(long id, String nome, String email) { super(id); this.nome = nome; this.email = email; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    @Override public String resumo() { return "%d | %s | %s".formatted(getId(), nome, email); }
}

