package br.com.fiap.enterprise_challenge3.dto.previsaodemanda;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricoPrevisaoDemandaResponse(
        LocalDateTime dataGeracao,

        Integer periodoHistoricoDias,
        Integer periodoPrevisaoDias,

        Integer totalPrevisoes,
        Integer totalOcorrenciasHistoricas,

        BigDecimal quantidadeTotalPrevista,

        Long demandasCriticas,
        Long demandasAltas,
        Long demandasMedias,
        Long demandasBaixas,

        BigDecimal maiorConfianca,

        String nomeModelo,
        String versaoModelo
) {
}