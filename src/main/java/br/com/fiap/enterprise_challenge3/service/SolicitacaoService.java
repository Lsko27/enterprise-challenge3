package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.EnderecoRequest;
import br.com.fiap.enterprise_challenge3.event.SolicitacaoCriadaEvent;
import org.springframework.context.ApplicationEventPublisher;
import br.com.fiap.enterprise_challenge3.dto.HistoricoSolicitacaoResponse;
import br.com.fiap.enterprise_challenge3.dto.SolicitacaoCreateRequest;
import br.com.fiap.enterprise_challenge3.dto.SolicitacaoResponse;
import br.com.fiap.enterprise_challenge3.model.Cidadao;
import br.com.fiap.enterprise_challenge3.model.Endereco;
import br.com.fiap.enterprise_challenge3.model.HistoricoSolicitacao;
import br.com.fiap.enterprise_challenge3.model.Solicitacao;
import br.com.fiap.enterprise_challenge3.model.Subservico;
import br.com.fiap.enterprise_challenge3.model.enums.StatusSolicitacao;
import br.com.fiap.enterprise_challenge3.repository.CidadaoRepository;
import br.com.fiap.enterprise_challenge3.repository.HistoricoSolicitacaoRepository;
import br.com.fiap.enterprise_challenge3.repository.SolicitacaoRepository;
import br.com.fiap.enterprise_challenge3.repository.SubservicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final CidadaoRepository cidadaoRepository;
    private final SubservicoRepository subservicoRepository;
    private final HistoricoSolicitacaoRepository historicoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SolicitacaoService(
            SolicitacaoRepository solicitacaoRepository,
            CidadaoRepository cidadaoRepository,
            SubservicoRepository subservicoRepository,
            HistoricoSolicitacaoRepository historicoRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.cidadaoRepository = cidadaoRepository;
        this.subservicoRepository = subservicoRepository;
        this.historicoRepository = historicoRepository;
        this.eventPublisher = eventPublisher;
    }

    public SolicitacaoResponse cadastrar(
            Long cidadaoId,
            SolicitacaoCreateRequest request
    ) {
        Cidadao cidadao =
                encontrarCidadaoAtivo(cidadaoId);

        Subservico subservico =
                encontrarSubservicoAtivo(
                        request.subservicoId()
                );

        Endereco endereco =
                criarEndereco(request.endereco());

        Solicitacao solicitacao =
                new Solicitacao(
                        request.titulo().trim(),
                        request.descricao().trim(),
                        request.urgencia(),
                        cidadao,
                        subservico,
                        endereco
                );

        Solicitacao solicitacaoSalva =
                solicitacaoRepository.save(
                        solicitacao
                );

        registrarHistorico(
                solicitacaoSalva,
                null,
                StatusSolicitacao.REGISTRADA,
                "Solicitação registrada pelo cidadão"
        );

        eventPublisher.publishEvent(
                new SolicitacaoCriadaEvent(
                        solicitacaoSalva.getId()
                )
        );

        return SolicitacaoResponse.fromEntity(
                solicitacaoSalva
        );
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listarMinhas(
            Long cidadaoId
    ) {
        return solicitacaoRepository
                .findAllByCidadao_IdOrderByDataAberturaDesc(cidadaoId)
                .stream()
                .map(SolicitacaoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponse buscarMinhaPorId(
            Long solicitacaoId,
            Long cidadaoId
    ) {
        Solicitacao solicitacao =
                encontrarSolicitacaoDoCidadao(
                        solicitacaoId,
                        cidadaoId
                );

        return SolicitacaoResponse.fromEntity(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<HistoricoSolicitacaoResponse> listarHistorico(
            Long solicitacaoId,
            Long cidadaoId
    ) {
        encontrarSolicitacaoDoCidadao(
                solicitacaoId,
                cidadaoId
        );

        return historicoRepository
                .findAllBySolicitacao_IdOrderByDataAlteracaoAsc(
                        solicitacaoId
                )
                .stream()
                .map(HistoricoSolicitacaoResponse::fromEntity)
                .toList();
    }

    public void cancelar(
            Long solicitacaoId,
            Long cidadaoId
    ) {
        Solicitacao solicitacao =
                encontrarSolicitacaoDoCidadao(
                        solicitacaoId,
                        cidadaoId
                );

        boolean podeCancelar =
                solicitacao.getStatus() == StatusSolicitacao.REGISTRADA
                        || solicitacao.getStatus()
                        == StatusSolicitacao.EM_TRIAGEM;

        if (!podeCancelar) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A solicitação não pode mais ser cancelada"
            );
        }

        StatusSolicitacao statusAnterior =
                solicitacao.getStatus();

        solicitacao.setStatus(StatusSolicitacao.CANCELADA);

        solicitacaoRepository.save(solicitacao);

        registrarHistorico(
                solicitacao,
                statusAnterior,
                StatusSolicitacao.CANCELADA,
                "Solicitação cancelada pelo cidadão"
        );
    }

    private Cidadao encontrarCidadaoAtivo(
            Long cidadaoId
    ) {
        Cidadao cidadao = cidadaoRepository
                .findById(cidadaoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Cidadão não encontrado"
                        )
                );

        if (!Boolean.TRUE.equals(cidadao.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cidadão desativado"
            );
        }

        return cidadao;
    }

    private Subservico encontrarSubservicoAtivo(
            Long subservicoId
    ) {
        Subservico subservico = subservicoRepository
                .findById(subservicoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Subserviço não encontrado"
                        )
                );

        if (!Boolean.TRUE.equals(subservico.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O subserviço está desativado"
            );
        }

        if (!Boolean.TRUE.equals(
                subservico.getCategoria().getAtivo()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A categoria está desativada"
            );
        }

        return subservico;
    }

    private Solicitacao encontrarSolicitacaoDoCidadao(
            Long solicitacaoId,
            Long cidadaoId
    ) {
        return solicitacaoRepository
                .findByIdAndCidadao_Id(
                        solicitacaoId,
                        cidadaoId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Solicitação não encontrada"
                        )
                );
    }

    private Endereco criarEndereco(
            EnderecoRequest request
    ) {
        return new Endereco(
                request.logradouro().trim(),
                normalizarTexto(request.numero()),
                normalizarTexto(request.complemento()),
                request.bairro().trim(),
                request.cidade().trim(),
                request.estado()
                        .trim()
                        .toUpperCase(Locale.ROOT),
                normalizarCep(request.cep()),
                request.latitude(),
                request.longitude()
        );
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

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }

    private String normalizarCep(String cep) {
        if (cep == null || cep.isBlank()) {
            return null;
        }

        return cep.replaceAll("\\D", "");
    }
}