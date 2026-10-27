package br.com.biblioteca.model;
import java.time.LocalDate;
public record Emprestimo(long id, String livro, String leitor, LocalDate realizadoEm,
                         LocalDate previstoEm, LocalDate devolvidoEm) {
    public String resumo() {
        String status = devolvidoEm != null ? "devolvido em " + devolvidoEm
            : previstoEm.isBefore(LocalDate.now()) ? "ATRASADO" : "aberto";
        return "%d | %s → %s | retirada %s | prazo %s | %s"
            .formatted(id, livro, leitor, realizadoEm, previstoEm, status);
    }
}

