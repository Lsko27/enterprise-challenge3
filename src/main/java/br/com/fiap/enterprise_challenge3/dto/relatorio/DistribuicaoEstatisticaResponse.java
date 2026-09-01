package br.com.fiap.enterprise_challenge3.dto.relatorio;

import java.math.BigDecimal;

public record DistribuicaoEstatisticaResponse(
        String codigo,
        String descricao,
        long quantidade,
        BigDecimal percentual
) {
}