package br.com.fiap.enterprise_challenge3.dto.previsaodemanda;

import java.time.LocalDateTime;

public record HistoricoDemandaIaItem(
        Long solicitacaoId,
        String bairro,
        Long categoriaId,
        String categoriaNome,
        Long subservicoId,
        String subservicoNome,
        LocalDateTime dataAbertura
) {
}