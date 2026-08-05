package br.com.fiap.enterprise_challenge3.dto.previsaodemanda;

import java.time.LocalDateTime;
import java.util.List;

public record PrevisaoDemandaIaResponse(
        LocalDateTime dataGeracao,
        int periodoHistoricoDias,
        int periodoPrevisaoDias,
        int totalRegistrosRecebidos,
        int totalRegistrosAnalisados,
        int totalPrevisoes,
        List<PrevisaoDemandaIaItem> previsoes
) {
}