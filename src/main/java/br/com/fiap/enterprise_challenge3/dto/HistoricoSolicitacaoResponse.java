package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.HistoricoSolicitacao;
import br.com.fiap.enterprise_challenge3.model.enums.StatusSolicitacao;

import java.time.LocalDateTime;

public record HistoricoSolicitacaoResponse(
        Long id,
        StatusSolicitacao statusAnterior,
        StatusSolicitacao statusNovo,
        String observacao,
        LocalDateTime dataAlteracao
) {

    public static HistoricoSolicitacaoResponse fromEntity(
            HistoricoSolicitacao historico
    ) {
        return new HistoricoSolicitacaoResponse(
                historico.getId(),
                historico.getStatusAnterior(),
                historico.getStatusNovo(),
                historico.getObservacao(),
                historico.getDataAlteracao()
        );
    }
}