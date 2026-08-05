package br.com.fiap.enterprise_challenge3.dto.previsaodemanda;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PrevisaoDemandaConsultaResponse(
        Long idPrevisao,

        String bairro,

        Long categoriaId,
        String categoriaNome,

        Long subservicoId,
        String subservicoNome,

        Integer periodoHistoricoDias,
        Integer periodoPrevisaoDias,

        Integer ocorrenciasHistoricas,

        BigDecimal mediaDiaria,
        BigDecimal tendenciaPercentual,
        BigDecimal quantidadePrevista,

        String tendencia,
        String nivelDemanda,

        BigDecimal confianca,
        String justificativa,

        String nomeModelo,
        String versaoModelo,

        LocalDateTime dataPrevisao
) {
}