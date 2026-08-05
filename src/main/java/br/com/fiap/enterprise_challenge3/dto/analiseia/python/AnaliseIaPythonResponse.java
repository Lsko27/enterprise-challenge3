package br.com.fiap.enterprise_challenge3.dto.analiseia.python;

import br.com.fiap.enterprise_challenge3.model.enums.NivelPrioridade;
import br.com.fiap.enterprise_challenge3.model.enums.NivelUrgencia;

import java.math.BigDecimal;

public record AnaliseIaPythonResponse(
        Long categoriaSugeridaId,
        Long subservicoSugeridoId,
        NivelUrgencia urgenciaSugerida,
        BigDecimal scorePrioridade,
        NivelPrioridade nivelPrioridade,
        BigDecimal confianca,
        String justificativa,
        String nomeModelo,
        String versaoModelo
) {
}