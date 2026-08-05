package br.com.fiap.enterprise_challenge3.dto.analiseia;

import br.com.fiap.enterprise_challenge3.model.AnaliseIa;
import br.com.fiap.enterprise_challenge3.model.enums.NivelPrioridade;
import br.com.fiap.enterprise_challenge3.model.enums.NivelUrgencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AnaliseIaResponse(
        Long id,
        Long solicitacaoId,

        Long categoriaSugeridaId,
        String categoriaSugeridaNome,

        Long subservicoSugeridoId,
        String subservicoSugeridoNome,

        NivelUrgencia urgenciaSugerida,
        BigDecimal scorePrioridade,
        NivelPrioridade nivelPrioridade,
        BigDecimal confianca,

        String justificativa,
        String nomeModelo,
        String versaoModelo,

        Boolean aceitaCidadao,
        LocalDateTime dataAnalise
) {

    public static AnaliseIaResponse fromEntity(
            AnaliseIa analise
    ) {
        return new AnaliseIaResponse(
                analise.getId(),

                analise.getSolicitacao() != null
                        ? analise.getSolicitacao().getId()
                        : null,

                analise.getCategoriaSugerida() != null
                        ? analise.getCategoriaSugerida().getId()
                        : null,

                analise.getCategoriaSugerida() != null
                        ? analise.getCategoriaSugerida().getNome()
                        : null,

                analise.getSubservicoSugerido() != null
                        ? analise.getSubservicoSugerido().getId()
                        : null,

                analise.getSubservicoSugerido() != null
                        ? analise.getSubservicoSugerido().getNome()
                        : null,

                analise.getUrgenciaSugerida(),
                analise.getScorePrioridade(),
                analise.getNivelPrioridade(),
                analise.getConfianca(),

                analise.getJustificativa(),
                analise.getNomeModelo(),
                analise.getVersaoModelo(),

                analise.getAceitaCidadao(),
                analise.getDataAnalise()
        );
    }
}