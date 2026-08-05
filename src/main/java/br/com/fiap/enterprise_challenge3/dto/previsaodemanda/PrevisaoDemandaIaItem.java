package br.com.fiap.enterprise_challenge3.dto.previsaodemanda;

public record PrevisaoDemandaIaItem(
        String bairro,

        Long categoriaId,
        String categoriaNome,

        Long subservicoId,
        String subservicoNome,

        int ocorrenciasHistoricas,
        int ocorrenciasPeriodoAnterior,
        int ocorrenciasPeriodoRecente,

        double mediaDiaria,
        double tendenciaPercentual,
        String tendencia,

        double quantidadePrevista,
        String nivelDemanda,
        double confianca,

        String justificativa,
        String nomeModelo,
        String versaoModelo
) {
}