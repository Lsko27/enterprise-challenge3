package br.com.fiap.enterprise_challenge3.dto.previsaodemanda;

import java.math.BigDecimal;

public record AnaliseRegressaoIaResponse(
        BigDecimal coeficienteCorrelacao,
        BigDecimal coeficienteDeterminacao,
        BigDecimal coeficienteAngular,
        BigDecimal intercepto,
        String equacao,
        String forcaCorrelacao,
        String tendencia,
        Integer quantidadeDias,
        BigDecimal variacaoEstimadaPercentual,
        String interpretacao
) {
}