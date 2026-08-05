package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.Solicitacao;

public record ServidorSolicitacaoResponse(
        Long cidadaoId,
        String cidadaoNome,
        SolicitacaoResponse solicitacao
) {

    public static ServidorSolicitacaoResponse fromEntity(
            Solicitacao solicitacao
    ) {
        return new ServidorSolicitacaoResponse(
                solicitacao.getCidadao().getId(),
                solicitacao.getCidadao().getNome(),
                SolicitacaoResponse.fromEntity(solicitacao)
        );
    }
}