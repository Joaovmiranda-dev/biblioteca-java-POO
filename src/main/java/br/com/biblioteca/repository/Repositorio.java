package br.com.biblioteca.repository;
import java.sql.SQLException;
import java.util.*;
public interface Repositorio<T> {
    long salvar(T entidade) throws SQLException;
    Optional<T> buscar(long id) throws SQLException;
    List<T> listar() throws SQLException;
    boolean excluir(long id) throws SQLException;
}

