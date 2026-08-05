package br.com.fiap.enterprise_challenge3.dto.previsaodemanda;

import java.util.List;

public record PrevisaoDemandaIaRequest(
        int periodoHistoricoDias,
        int periodoPrevisaoDias,
        int minimoOcorrencias,
        List<HistoricoDemandaIaItem> historico
) {
}