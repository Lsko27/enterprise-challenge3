package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.Solicitacao;
import br.com.fiap.enterprise_challenge3.model.enums.NivelUrgencia;
import br.com.fiap.enterprise_challenge3.model.enums.StatusSolicitacao;

import java.time.LocalDateTime;

public record SolicitacaoResponse(
        Long id,
        String titulo,
        String descricao,
        StatusSolicitacao status,
        NivelUrgencia urgencia,
        LocalDateTime dataAbertura,
        LocalDateTime dataAtualizacao,
        Long subservicoId,
        String subservicoNome,
        Long categoriaId,
        String categoriaNome,
        EnderecoResponse endereco
) {

    public static SolicitacaoResponse fromEntity(
            Solicitacao solicitacao
    ) {
        return new SolicitacaoResponse(
                solicitacao.getId(),
                solicitacao.getTitulo(),
                solicitacao.getDescricao(),
                solicitacao.getStatus(),
                solicitacao.getUrgencia(),
                solicitacao.getDataAbertura(),
                solicitacao.getDataAtualizacao(),
                solicitacao.getSubservico().getId(),
                solicitacao.getSubservico().getNome(),
                solicitacao.getSubservico()
                        .getCategoria()
                        .getId(),
                solicitacao.getSubservico()
                        .getCategoria()
                        .getNome(),
                EnderecoResponse.fromEntity(
                        solicitacao.getEndereco()
                )
        );
    }
}