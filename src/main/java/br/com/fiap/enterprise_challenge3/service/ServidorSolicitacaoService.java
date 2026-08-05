package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.AtualizarStatusSolicitacaoRequest;
import br.com.fiap.enterprise_challenge3.dto.HistoricoSolicitacaoResponse;
import br.com.fiap.enterprise_challenge3.dto.ServidorSolicitacaoResponse;
import br.com.fiap.enterprise_challenge3.model.HistoricoSolicitacao;
import br.com.fiap.enterprise_challenge3.model.Solicitacao;
import br.com.fiap.enterprise_challenge3.model.enums.StatusSolicitacao;
import br.com.fiap.enterprise_challenge3.repository.HistoricoSolicitacaoRepository;
import br.com.fiap.enterprise_challenge3.repository.SolicitacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ServidorSolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final HistoricoSolicitacaoRepository historicoRepository;
    private final NotificacaoService notificacaoService;

    public ServidorSolicitacaoService(
            SolicitacaoRepository solicitacaoRepository,
            HistoricoSolicitacaoRepository historicoRepository,
            NotificacaoService notificacaoService
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.historicoRepository = historicoRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public List<ServidorSolicitacaoResponse> listarTodas() {
        return solicitacaoRepository
                .listarTodasComDetalhes()
                .stream()
                .map(ServidorSolicitacaoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServidorSolicitacaoResponse buscarPorId(
            Long solicitacaoId
    ) {
        Solicitacao solicitacao =
                encontrarSolicitacao(solicitacaoId);

        return ServidorSolicitacaoResponse.fromEntity(
                solicitacao
        );
    }

    @Transactional(readOnly = true)
    public List<HistoricoSolicitacaoResponse> listarHistorico(
            Long solicitacaoId
    ) {
        encontrarSolicitacao(solicitacaoId);

        return historicoRepository
                .findAllBySolicitacao_IdOrderByDataAlteracaoAsc(
                        solicitacaoId
                )
                .stream()
                .map(HistoricoSolicitacaoResponse::fromEntity)
                .toList();
    }

    public ServidorSolicitacaoResponse atualizarStatus(
            Long solicitacaoId,
            AtualizarStatusSolicitacaoRequest request
    ) {
        Solicitacao solicitacao =
                encontrarSolicitacao(solicitacaoId);

        StatusSolicitacao statusAnterior =
                solicitacao.getStatus();

        StatusSolicitacao statusNovo =
                request.novoStatus();

        if (statusAnterior == statusNovo) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A solicitação já possui esse status"
            );
        }

        validarStatusPermitidoParaServidor(statusNovo);

        validarTransicao(
                statusAnterior,
                statusNovo
        );

        solicitacao.setStatus(statusNovo);

        Solicitacao solicitacaoSalva =
                solicitacaoRepository.saveAndFlush(
                        solicitacao
                );

        String observacao =
                request.observacao().trim();

        registrarHistorico(
                solicitacaoSalva,
                statusAnterior,
                statusNovo,
                observacao
        );

        notificacaoService.notificarAlteracaoStatus(
                solicitacaoSalva,
                statusAnterior,
                statusNovo,
                observacao
        );

        return ServidorSolicitacaoResponse.fromEntity(
                solicitacaoSalva
        );
    }

    private Solicitacao encontrarSolicitacao(
            Long solicitacaoId
    ) {
        return solicitacaoRepository
                .buscarPorIdComDetalhes(solicitacaoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Solicitação não encontrada"
                        )
                );
    }

    private void validarStatusPermitidoParaServidor(
            StatusSolicitacao statusNovo
    ) {
        if (statusNovo == StatusSolicitacao.REGISTRADA
                || statusNovo == StatusSolicitacao.CANCELADA) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O servidor não pode definir esse status"
            );
        }
    }

    private void validarTransicao(
            StatusSolicitacao statusAnterior,
            StatusSolicitacao statusNovo
    ) {
        boolean transicaoPermitida =
                switch (statusAnterior) {

                    case REGISTRADA ->
                            statusNovo
                                    == StatusSolicitacao.EM_TRIAGEM;

                    case EM_TRIAGEM ->
                            statusNovo
                                    == StatusSolicitacao.EM_ANDAMENTO
                                    || statusNovo
                                    == StatusSolicitacao
                                    .AGUARDANDO_INFORMACOES;

                    case EM_ANDAMENTO ->
                            statusNovo
                                    == StatusSolicitacao.CONCLUIDA
                                    || statusNovo
                                    == StatusSolicitacao
                                    .AGUARDANDO_INFORMACOES;

                    case AGUARDANDO_INFORMACOES ->
                            statusNovo
                                    == StatusSolicitacao.EM_TRIAGEM
                                    || statusNovo
                                    == StatusSolicitacao.EM_ANDAMENTO;

                    case CONCLUIDA, CANCELADA -> false;
                };

        if (!transicaoPermitida) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Transição de status não permitida: "
                            + statusAnterior
                            + " para "
                            + statusNovo
            );
        }
    }

    private void registrarHistorico(
            Solicitacao solicitacao,
            StatusSolicitacao statusAnterior,
            StatusSolicitacao statusNovo,
            String observacao
    ) {
        HistoricoSolicitacao historico =
                new HistoricoSolicitacao(
                        solicitacao,
                        statusAnterior,
                        statusNovo,
                        observacao
                );

        historicoRepository.save(historico);
    }
}