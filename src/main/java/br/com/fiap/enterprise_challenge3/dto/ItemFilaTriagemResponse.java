package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.Solicitacao;
import br.com.fiap.enterprise_challenge3.model.enums.NivelUrgencia;

import java.time.LocalDateTime;

public record ItemFilaTriagemResponse(
        int posicao,
        NivelUrgencia urgencia,
        LocalDateTime dataAbertura,
        ServidorSolicitacaoResponse solicitacao
) {

    public static ItemFilaTriagemResponse fromEntity(
            int posicao,
            Solicitacao solicitacao
    ) {
        return new ItemFilaTriagemResponse(
                posicao,
                solicitacao.getUrgencia(),
                solicitacao.getDataAbertura(),
                ServidorSolicitacaoResponse.fromEntity(
                        solicitacao
                )
        );
    }
}