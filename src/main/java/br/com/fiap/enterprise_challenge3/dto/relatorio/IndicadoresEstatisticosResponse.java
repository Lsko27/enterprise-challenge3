package br.com.fiap.enterprise_challenge3.dto.relatorio;

import java.math.BigDecimal;
import java.util.List;

public record IndicadoresEstatisticosResponse(
        long totalSolicitacoes,
        long solicitacoesEmAberto,
        long solicitacoesConcluidas,
        long solicitacoesCanceladas,
        BigDecimal taxaConclusaoPercentual,
        BigDecimal percentualAltaOuCritica,
        BigDecimal mediaDiaria,
        BigDecimal medianaDiaria,
        List<Long> modasDiarias,
        BigDecimal varianciaPopulacional,
        BigDecimal desvioPadraoPopulacional,
        BigDecimal coeficienteVariacaoPercentual,
        BigDecimal tempoMedioResolucaoHoras
) {
}