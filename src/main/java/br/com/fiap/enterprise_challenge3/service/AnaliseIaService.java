package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.analiseia.AnaliseIaResponse;
import br.com.fiap.enterprise_challenge3.dto.analiseia.python.AnaliseIaPythonRequest;
import br.com.fiap.enterprise_challenge3.dto.analiseia.python.AnaliseIaPythonResponse;
import br.com.fiap.enterprise_challenge3.dto.analiseia.python.CategoriaIaItem;
import br.com.fiap.enterprise_challenge3.dto.analiseia.python.SubservicoIaItem;
import br.com.fiap.enterprise_challenge3.model.AnaliseIa;
import br.com.fiap.enterprise_challenge3.model.Categoria;
import br.com.fiap.enterprise_challenge3.model.Solicitacao;
import br.com.fiap.enterprise_challenge3.model.Subservico;
import br.com.fiap.enterprise_challenge3.repository.AnaliseIaRepository;
import br.com.fiap.enterprise_challenge3.repository.CategoriaRepository;
import br.com.fiap.enterprise_challenge3.repository.SolicitacaoRepository;
import br.com.fiap.enterprise_challenge3.repository.SubservicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AnaliseIaService {

    private final AnaliseIaRepository analiseIaRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubservicoRepository subservicoRepository;
    private final AnaliseIaPythonClient analiseIaPythonClient;

    public AnaliseIaService(
            AnaliseIaRepository analiseIaRepository,
            SolicitacaoRepository solicitacaoRepository,
            CategoriaRepository categoriaRepository,
            SubservicoRepository subservicoRepository,
            AnaliseIaPythonClient analiseIaPythonClient
    ) {
        this.analiseIaRepository = analiseIaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.subservicoRepository = subservicoRepository;
        this.analiseIaPythonClient = analiseIaPythonClient;
    }

    @Transactional
    public AnaliseIaResponse gerarAnalise(
            Long solicitacaoId
    ) {
        Solicitacao solicitacao =
                solicitacaoRepository
                        .findById(solicitacaoId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Solicitação não encontrada"
                                )
                        );

        AnaliseIaPythonRequest request =
                criarRequestPython(solicitacao);

        AnaliseIaPythonResponse resultado =
                analiseIaPythonClient.analisar(request);

        validarResultado(resultado);

        Categoria categoria =
                categoriaRepository
                        .findById(
                                resultado.categoriaSugeridaId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "A IA sugeriu uma categoria inexistente"
                                )
                        );

        Subservico subservico =
                subservicoRepository
                        .findById(
                                resultado.subservicoSugeridoId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY,
                                        "A IA sugeriu um subserviço inexistente"
                                )
                        );

        validarRelacionamento(
                categoria,
                subservico
        );

        AnaliseIa analise = new AnaliseIa();

        analise.setSolicitacao(solicitacao);
        analise.setCategoriaSugerida(categoria);
        analise.setSubservicoSugerido(subservico);
        analise.setUrgenciaSugerida(
                resultado.urgenciaSugerida()
        );
        analise.setScorePrioridade(
                resultado.scorePrioridade()
        );
        analise.setNivelPrioridade(
                resultado.nivelPrioridade()
        );
        analise.setConfianca(
                resultado.confianca()
        );
        analise.setJustificativa(
                resultado.justificativa()
        );
        analise.setNomeModelo(
                resultado.nomeModelo()
        );
        analise.setVersaoModelo(
                resultado.versaoModelo()
        );
        analise.setAceitaCidadao(false);

        AnaliseIa analiseSalva =
                analiseIaRepository.save(analise);

        return AnaliseIaResponse.fromEntity(
                analiseSalva
        );
    }

    @Transactional
    public void gerarAnaliseInicial(
            Long solicitacaoId
    ) {
        validarSolicitacao(solicitacaoId);

        boolean jaPossuiAnalise =
                analiseIaRepository
                        .existsBySolicitacao_Id(
                                solicitacaoId
                        );

        if (jaPossuiAnalise) {
            return;
        }

        gerarAnalise(solicitacaoId);
    }

    @Transactional(readOnly = true)
    public AnaliseIaResponse buscarAnaliseMaisRecente(
            Long solicitacaoId
    ) {
        validarSolicitacao(solicitacaoId);

        AnaliseIa analise =
                analiseIaRepository
                        .findFirstBySolicitacao_IdOrderByDataAnaliseDesc(
                                solicitacaoId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Nenhuma análise de IA encontrada para esta solicitação"
                                )
                        );

        return AnaliseIaResponse.fromEntity(analise);
    }

    @Transactional(readOnly = true)
    public List<AnaliseIaResponse> listarHistorico(
            Long solicitacaoId
    ) {
        validarSolicitacao(solicitacaoId);

        return analiseIaRepository
                .findBySolicitacao_IdOrderByDataAnaliseDesc(
                        solicitacaoId
                )
                .stream()
                .map(AnaliseIaResponse::fromEntity)
                .toList();
    }

    private AnaliseIaPythonRequest criarRequestPython(
            Solicitacao solicitacao
    ) {
        List<CategoriaIaItem> categorias =
                categoriaRepository
                        .findAll()
                        .stream()
                        .map(categoria ->
                                new CategoriaIaItem(
                                        categoria.getId(),
                                        categoria.getNome()
                                )
                        )
                        .toList();

        List<SubservicoIaItem> subservicos =
                subservicoRepository
                        .findAll()
                        .stream()
                        .map(subservico ->
                                new SubservicoIaItem(
                                        subservico.getId(),
                                        subservico
                                                .getCategoria()
                                                .getId(),
                                        subservico.getNome()
                                )
                        )
                        .toList();

        return new AnaliseIaPythonRequest(
                solicitacao.getId(),
                solicitacao.getTitulo(),
                solicitacao.getDescricao(),
                categorias,
                subservicos
        );
    }

    private void validarResultado(
            AnaliseIaPythonResponse resultado
    ) {
        if (resultado.categoriaSugeridaId() == null) {
            throw respostaInvalida(
                    "A IA não retornou a categoria sugerida"
            );
        }

        if (resultado.subservicoSugeridoId() == null) {
            throw respostaInvalida(
                    "A IA não retornou o subserviço sugerido"
            );
        }

        if (resultado.urgenciaSugerida() == null) {
            throw respostaInvalida(
                    "A IA não retornou a urgência sugerida"
            );
        }

        if (resultado.nivelPrioridade() == null) {
            throw respostaInvalida(
                    "A IA não retornou o nível de prioridade"
            );
        }

        validarFaixa(
                resultado.scorePrioridade(),
                BigDecimal.ZERO,
                new BigDecimal("100"),
                "score de prioridade"
        );

        validarFaixa(
                resultado.confianca(),
                BigDecimal.ZERO,
                BigDecimal.ONE,
                "confiança"
        );
    }

    private void validarFaixa(
            BigDecimal valor,
            BigDecimal minimo,
            BigDecimal maximo,
            String nomeCampo
    ) {
        if (
                valor == null
                        || valor.compareTo(minimo) < 0
                        || valor.compareTo(maximo) > 0
        ) {
            throw respostaInvalida(
                    "Valor inválido para " + nomeCampo
            );
        }
    }

    private void validarRelacionamento(
            Categoria categoria,
            Subservico subservico
    ) {
        if (
                subservico.getCategoria() == null
                        || !subservico
                        .getCategoria()
                        .getId()
                        .equals(categoria.getId())
        ) {
            throw respostaInvalida(
                    "O subserviço sugerido não pertence à categoria sugerida"
            );
        }
    }

    private ResponseStatusException respostaInvalida(
            String mensagem
    ) {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                mensagem
        );
    }

    private void validarSolicitacao(
            Long solicitacaoId
    ) {
        if (!solicitacaoRepository.existsById(solicitacaoId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Solicitação não encontrada"
            );
        }
    }
}