package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.NotificacaoResponse;
import br.com.fiap.enterprise_challenge3.dto.NotificacoesMarcadasResponse;
import br.com.fiap.enterprise_challenge3.dto.QuantidadeNotificacoesResponse;
import br.com.fiap.enterprise_challenge3.model.Notificacao;
import br.com.fiap.enterprise_challenge3.model.Solicitacao;
import br.com.fiap.enterprise_challenge3.model.enums.StatusSolicitacao;
import br.com.fiap.enterprise_challenge3.repository.NotificacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository
    ) {
        this.notificacaoRepository =
                notificacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listarMinhas(
            Long cidadaoId
    ) {
        return notificacaoRepository
                .findAllByCidadao_IdOrderByDataCriacaoDesc(
                        cidadaoId
                )
                .stream()
                .map(NotificacaoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuantidadeNotificacoesResponse contarNaoLidas(
            Long cidadaoId
    ) {
        long quantidade =
                notificacaoRepository
                        .countByCidadao_IdAndLidaFalse(
                                cidadaoId
                        );

        return new QuantidadeNotificacoesResponse(
                quantidade
        );
    }

    public NotificacaoResponse marcarComoLida(
            Long notificacaoId,
            Long cidadaoId
    ) {
        Notificacao notificacao =
                notificacaoRepository
                        .findByIdAndCidadao_Id(
                                notificacaoId,
                                cidadaoId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Notificação não encontrada"
                                )
                        );

        notificacao.marcarComoLida();

        Notificacao notificacaoSalva =
                notificacaoRepository.save(notificacao);

        return NotificacaoResponse.fromEntity(
                notificacaoSalva
        );
    }

    public NotificacoesMarcadasResponse marcarTodasComoLidas(
            Long cidadaoId
    ) {
        int quantidade =
                notificacaoRepository
                        .marcarTodasComoLidas(
                                cidadaoId,
                                LocalDateTime.now()
                        );

        return new NotificacoesMarcadasResponse(
                quantidade
        );
    }

    public void notificarAlteracaoStatus(
            Solicitacao solicitacao,
            StatusSolicitacao statusAnterior,
            StatusSolicitacao statusNovo,
            String observacao
    ) {
        String titulo = criarTitulo(statusNovo);

        String mensagem = criarMensagem(
                solicitacao,
                statusAnterior,
                statusNovo,
                observacao
        );

        Notificacao notificacao = new Notificacao(
                titulo,
                mensagem,
                solicitacao.getCidadao(),
                solicitacao
        );

        notificacaoRepository.save(notificacao);
    }

    private String criarTitulo(
            StatusSolicitacao status
    ) {
        return switch (status) {
            case EM_TRIAGEM ->
                    "Solicitação em triagem";

            case EM_ANDAMENTO ->
                    "Atendimento iniciado";

            case AGUARDANDO_INFORMACOES ->
                    "Informações adicionais necessárias";

            case CONCLUIDA ->
                    "Solicitação concluída";

            case CANCELADA ->
                    "Solicitação cancelada";

            case REGISTRADA ->
                    "Solicitação registrada";
        };
    }

    private String criarMensagem(
            Solicitacao solicitacao,
            StatusSolicitacao statusAnterior,
            StatusSolicitacao statusNovo,
            String observacao
    ) {
        String mensagem =
                "A solicitação #" + solicitacao.getId()
                        + " foi atualizada de "
                        + formatarStatus(statusAnterior)
                        + " para "
                        + formatarStatus(statusNovo)
                        + ".";

        if (observacao != null &&
                !observacao.isBlank()) {

            mensagem += " Observação: "
                    + observacao.trim();
        }

        return mensagem;
    }

    private String formatarStatus(
            StatusSolicitacao status
    ) {
        if (status == null) {
            return "sem status anterior";
        }

        return switch (status) {
            case REGISTRADA -> "Registrada";
            case EM_TRIAGEM -> "Em triagem";
            case EM_ANDAMENTO -> "Em andamento";
            case AGUARDANDO_INFORMACOES ->
                    "Aguardando informações";
            case CONCLUIDA -> "Concluída";
            case CANCELADA -> "Cancelada";
        };
    }
}