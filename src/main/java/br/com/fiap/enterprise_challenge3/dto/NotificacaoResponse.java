package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.Notificacao;

import java.time.LocalDateTime;

public record NotificacaoResponse(
        Long id,
        Long solicitacaoId,
        String titulo,
        String mensagem,
        Boolean lida,
        LocalDateTime dataCriacao,
        LocalDateTime dataLeitura
) {

    public static NotificacaoResponse fromEntity(
            Notificacao notificacao
    ) {
        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getSolicitacao().getId(),
                notificacao.getTitulo(),
                notificacao.getMensagem(),
                notificacao.getLida(),
                notificacao.getDataCriacao(),
                notificacao.getDataLeitura()
        );
    }
}