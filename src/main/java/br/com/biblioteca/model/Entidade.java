package br.com.biblioteca.model;
public abstract class Entidade {
    private final long id;
    protected Entidade(long id) { this.id = id; }
    public final long getId() { return id; }
    public abstract String resumo();
}

