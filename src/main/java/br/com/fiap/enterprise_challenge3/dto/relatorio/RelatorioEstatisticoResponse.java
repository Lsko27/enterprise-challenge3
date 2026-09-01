package br.com.fiap.enterprise_challenge3.dto.relatorio;

import java.time.LocalDate;
import java.util.List;

public record RelatorioEstatisticoResponse(
        LocalDate periodoInicio,
        LocalDate periodoFim,
        IndicadoresEstatisticosResponse indicadores,
        List<DistribuicaoEstatisticaResponse> porCategoria,
        List<DistribuicaoEstatisticaResponse> porStatus,
        List<DistribuicaoEstatisticaResponse> porUrgencia,
        List<SerieTemporalDiariaResponse> evolucaoDiaria,
        List<String> analisesESugestoes
) {
}