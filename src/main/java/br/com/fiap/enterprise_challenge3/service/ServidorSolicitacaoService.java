package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.AtualizarStatusSolicitacaoRequest;
import br.com.fiap.enterprise_challenge3.dto.HistoricoSolicitacaoResponse;
import br.com.fiap.enterprise_challenge3.dto.ItemFilaTriagemResponse;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

@Service
@Transactional
public class ServidorSolicitacaoService {

    private static final List<StatusSolicitacao>
            STATUS_DA_FILA = List.of(
            StatusSolicitacao.REGISTRADA,
            StatusSolicitacao.EM_TRIAGEM
    );

    private static final Comparator<Solicitacao>
            COMPARADOR_DA_FILA = Comparator
            .comparingInt(
                    (Solicitacao solicitacao) ->
                            solicitacao
                                    .getUrgencia()
                                    .getPrioridade()
            )
            .reversed()
            .thenComparing(
                    Solicitacao::getDataAbertura
            )
            .thenComparing(
                    Solicitacao::getId
            );

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
    public List<ItemFilaTriagemResponse> listarFilaTriagem() {
        List<Solicitacao> solicitacoesPendentes =
                solicitacaoRepository.findAllByStatusIn(
                        STATUS_DA_FILA
                );

        Queue<Solicitacao> fila =
                new PriorityQueue<>(
                        COMPARADOR_DA_FILA
                );

        fila.addAll(solicitacoesPendentes);

        List<ItemFilaTriagemResponse> itensOrdenados =
                new ArrayList<>(fila.size());

        int posicao = 1;

        while (!fila.isEmpty()) {
            Solicitacao proximaSolicitacao =
                    fila.remove();

            itensOrdenados.add(
                    ItemFilaTriagemResponse.fromEntity(
                            posicao,
                            proximaSolicitacao
                    )
            );

            posicao++;
        }

        return List.copyOf(itensOrdenados);
    }

    @Transactional(readOnly = true)
    public ServidorSolicitacaoResponse buscarPorId(
            Long solicitacaoId
    ) {
        Solicitacao solicitacao =
                encontrarSolicitacao(
                        solicitacaoId
                );

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
                .map(
                        HistoricoSolicitacaoResponse::fromEntity
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoSolicitacaoResponse>
    listarHistoricoReverso(
            Long solicitacaoId
    ) {
        encontrarSolicitacao(solicitacaoId);

        List<HistoricoSolicitacao> historicoCronologico =
                historicoRepository
                        .findAllBySolicitacao_IdOrderByDataAlteracaoAsc(
                                solicitacaoId
                        );

        Deque<HistoricoSolicitacaoResponse> pilha =
                new ArrayDeque<>();

        historicoCronologico
                .stream()
                .map(
                        HistoricoSolicitacaoResponse::fromEntity
                )
                .forEach(pilha::push);

        List<HistoricoSolicitacaoResponse> historicoReverso =
                new ArrayList<>(
                        pilha.size()
                );

        while (!pilha.isEmpty()) {
            historicoReverso.add(
                    pilha.pop()
            );
        }

        return List.copyOf(historicoReverso);
    }

    public ServidorSolicitacaoResponse atualizarStatus(
            Long solicitacaoId,
            AtualizarStatusSolicitacaoRequest request
    ) {
        Solicitacao solicitacao =
                encontrarSolicitacao(
                        solicitacaoId
                );

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

        validarStatusPermitidoParaServidor(
                statusNovo
        );

        validarTransicao(
                statusAnterior,
                statusNovo
        );

        solicitacao.setStatus(
                statusNovo
        );

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
                .buscarPorIdComDetalhes(
                        solicitacaoId
                )
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
        if (
                statusNovo == StatusSolicitacao.REGISTRADA
                        || statusNovo
                        == StatusSolicitacao.CANCELADA
        ) {
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

        historicoRepository.save(
                historico
        );
    }
}