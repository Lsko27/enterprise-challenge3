package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.client.PrevisaoDemandaPythonClient;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.GerarPrevisaoDemandaRequest;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.HistoricoDemandaIaItem;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.PrevisaoDemandaConsultaResponse;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.PrevisaoDemandaIaItem;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.PrevisaoDemandaIaRequest;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.PrevisaoDemandaIaResponse;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.ResumoPrevisaoDemandaResponse;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.HistoricoPrevisaoDemandaResponse;
import br.com.fiap.enterprise_challenge3.model.PrevisaoDemanda;
import br.com.fiap.enterprise_challenge3.repository.ConsultaPrevisaoDemandaRepository;
import br.com.fiap.enterprise_challenge3.repository.HistoricoDemandaRepository;
import br.com.fiap.enterprise_challenge3.repository.PrevisaoDemandaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrevisaoDemandaService {

    private static final int HISTORICO_PADRAO = 90;
    private static final int PREVISAO_PADRAO = 30;
    private static final int MINIMO_PADRAO = 2;

    private final HistoricoDemandaRepository
            historicoDemandaRepository;

    private final PrevisaoDemandaRepository
            previsaoDemandaRepository;

    private final PrevisaoDemandaPythonClient
            previsaoDemandaPythonClient;

    private final ConsultaPrevisaoDemandaRepository
            consultaPrevisaoDemandaRepository;

    public PrevisaoDemandaService(
            HistoricoDemandaRepository
                    historicoDemandaRepository,

            PrevisaoDemandaRepository
                    previsaoDemandaRepository,

            PrevisaoDemandaPythonClient
                    previsaoDemandaPythonClient,

            ConsultaPrevisaoDemandaRepository
                    consultaPrevisaoDemandaRepository
    ) {
        this.historicoDemandaRepository =
                historicoDemandaRepository;

        this.previsaoDemandaRepository =
                previsaoDemandaRepository;

        this.previsaoDemandaPythonClient =
                previsaoDemandaPythonClient;

        this.consultaPrevisaoDemandaRepository =
                consultaPrevisaoDemandaRepository;
    }

    @Transactional
    public synchronized PrevisaoDemandaIaResponse gerarPrevisoes(
            GerarPrevisaoDemandaRequest parametros
    ) {
        int periodoHistorico = obterValorOuPadrao(
                parametros.periodoHistoricoDias(),
                HISTORICO_PADRAO
        );

        int periodoPrevisao = obterValorOuPadrao(
                parametros.periodoPrevisaoDias(),
                PREVISAO_PADRAO
        );

        int minimoOcorrencias = obterValorOuPadrao(
                parametros.minimoOcorrencias(),
                MINIMO_PADRAO
        );

        validarParametros(
                periodoHistorico,
                periodoPrevisao,
                minimoOcorrencias
        );

        validarGeracaoDuplicada(
                parametros.deveForcarGeracao()
        );

        List<HistoricoDemandaIaItem> historico =
                historicoDemandaRepository
                        .buscarHistorico(
                                periodoHistorico
                        );

        if (historico.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Não existem solicitações no período "
                            + "histórico informado."
            );
        }

        PrevisaoDemandaIaRequest requestIa =
                new PrevisaoDemandaIaRequest(
                        periodoHistorico,
                        periodoPrevisao,
                        minimoOcorrencias,
                        historico
                );

        PrevisaoDemandaIaResponse respostaIa =
                previsaoDemandaPythonClient.prever(
                        requestIa
                );

        if (respostaIa.previsoes() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "A IA não retornou a lista de previsões."
            );
        }

        List<PrevisaoDemanda> entidades =
                respostaIa.previsoes()
                        .stream()
                        .map(item ->
                                converterParaEntidade(
                                        item,
                                        respostaIa
                                )
                        )
                        .toList();

        previsaoDemandaRepository.saveAll(
                entidades
        );

        previsaoDemandaRepository.flush();

        return respostaIa;
    }

    @Transactional(readOnly = true)
    public List<PrevisaoDemandaConsultaResponse>
    consultarUltimaGeracao() {

        List<PrevisaoDemandaConsultaResponse> previsoes =
                consultaPrevisaoDemandaRepository
                        .buscarUltimaGeracao();

        validarExistenciaPrevisoes(
                previsoes
        );

        return previsoes;
    }

    @Transactional(readOnly = true)
    public ResumoPrevisaoDemandaResponse
    consultarResumoUltimaGeracao() {

        List<PrevisaoDemandaConsultaResponse> previsoes =
                consultaPrevisaoDemandaRepository
                        .buscarUltimaGeracao();

        validarExistenciaPrevisoes(
                previsoes
        );

        BigDecimal quantidadeTotalPrevista =
                previsoes.stream()
                        .map(item ->
                                valorOuZero(
                                        item.quantidadePrevista()
                                )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        int totalOcorrenciasHistoricas =
                previsoes.stream()
                        .mapToInt(item ->
                                item.ocorrenciasHistoricas() == null
                                        ? 0
                                        : item.ocorrenciasHistoricas()
                        )
                        .sum();

        long demandasCriticas =
                contarNivel(
                        previsoes,
                        "CRITICA"
                );

        long demandasAltas =
                contarNivel(
                        previsoes,
                        "ALTA"
                );

        long demandasMedias =
                contarNivel(
                        previsoes,
                        "MEDIA"
                );

        long demandasBaixas =
                contarNivel(
                        previsoes,
                        "BAIXA"
                );

        Map<String, BigDecimal> totaisPorBairro =
                new HashMap<>();

        for (
                PrevisaoDemandaConsultaResponse item
                : previsoes
        ) {
            totaisPorBairro.merge(
                    item.bairro(),
                    valorOuZero(
                            item.quantidadePrevista()
                    ),
                    BigDecimal::add
            );
        }

        Map.Entry<String, BigDecimal>
                maiorBairro = totaisPorBairro
                .entrySet()
                .stream()
                .max(
                        Map.Entry.comparingByValue()
                )
                .orElseThrow();

        PrevisaoDemandaConsultaResponse
                maiorCrescimento = previsoes
                .stream()
                .max(
                        (primeiro, segundo) ->
                                valorOuZero(
                                        primeiro
                                                .tendenciaPercentual()
                                ).compareTo(
                                        valorOuZero(
                                                segundo
                                                        .tendenciaPercentual()
                                        )
                                )
                )
                .orElseThrow();

        BigDecimal maiorConfianca =
                previsoes.stream()
                        .map(item ->
                                valorOuZero(
                                        item.confianca()
                                )
                        )
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        PrevisaoDemandaConsultaResponse primeira =
                previsoes.getFirst();

        return new ResumoPrevisaoDemandaResponse(
                primeira.dataPrevisao(),

                primeira.periodoHistoricoDias(),
                primeira.periodoPrevisaoDias(),

                previsoes.size(),
                totalOcorrenciasHistoricas,

                quantidadeTotalPrevista,

                demandasCriticas,
                demandasAltas,
                demandasMedias,
                demandasBaixas,

                maiorBairro.getKey(),
                maiorBairro.getValue(),

                maiorCrescimento.bairro(),
                maiorCrescimento.categoriaNome(),
                maiorCrescimento.subservicoNome(),
                maiorCrescimento.tendenciaPercentual(),

                maiorConfianca
        );
    }

    @Transactional(readOnly = true)
    public List<HistoricoPrevisaoDemandaResponse>
    consultarHistoricoGeracoes() {

        List<HistoricoPrevisaoDemandaResponse> historico =
                consultaPrevisaoDemandaRepository
                        .buscarHistoricoGeracoes();

        if (
                historico == null
                        || historico.isEmpty()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Nenhuma geração de previsão "
                            + "foi encontrada."
            );
        }

        return historico;
    }

    private PrevisaoDemanda converterParaEntidade(
            PrevisaoDemandaIaItem item,
            PrevisaoDemandaIaResponse resposta
    ) {
        return new PrevisaoDemanda(
                item.categoriaId(),
                item.subservicoId(),
                item.bairro(),

                resposta.periodoHistoricoDias(),
                resposta.periodoPrevisaoDias(),

                item.ocorrenciasHistoricas(),

                BigDecimal.valueOf(
                        item.mediaDiaria()
                ),

                BigDecimal.valueOf(
                        item.tendenciaPercentual()
                ),

                BigDecimal.valueOf(
                        item.quantidadePrevista()
                ),

                item.tendencia(),
                item.nivelDemanda(),

                BigDecimal.valueOf(
                        item.confianca()
                ),

                item.justificativa(),
                item.nomeModelo(),
                item.versaoModelo(),

                resposta.dataGeracao()
        );
    }

    private long contarNivel(
            List<PrevisaoDemandaConsultaResponse> previsoes,
            String nivel
    ) {
        return previsoes.stream()
                .filter(item ->
                        nivel.equalsIgnoreCase(
                                item.nivelDemanda()
                        )
                )
                .count();
    }

    private BigDecimal valorOuZero(
            BigDecimal valor
    ) {
        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private void validarExistenciaPrevisoes(
            List<PrevisaoDemandaConsultaResponse> previsoes
    ) {
        if (
                previsoes == null
                        || previsoes.isEmpty()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Nenhuma previsão de demanda "
                            + "foi gerada até o momento."
            );
        }
    }

    private int obterValorOuPadrao(
            Integer valor,
            int valorPadrao
    ) {
        return valor == null
                ? valorPadrao
                : valor;
    }

    private void validarParametros(
            int periodoHistorico,
            int periodoPrevisao,
            int minimoOcorrencias
    ) {
        if (
                periodoHistorico < 30
                        || periodoHistorico > 730
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O período histórico deve estar "
                            + "entre 30 e 730 dias."
            );
        }

        if (
                periodoPrevisao < 7
                        || periodoPrevisao > 90
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O período de previsão deve estar "
                            + "entre 7 e 90 dias."
            );
        }

        if (
                minimoOcorrencias < 1
                        || minimoOcorrencias > 100
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O mínimo de ocorrências deve estar "
                            + "entre 1 e 100."
            );
        }
    }

    private void validarGeracaoDuplicada(
            boolean forcarGeracao
    ) {
        if (forcarGeracao) {
            return;
        }

        Optional<LocalDateTime> dataUltimaGeracao =
                consultaPrevisaoDemandaRepository
                        .buscarDataUltimaGeracao();

        if (dataUltimaGeracao.isEmpty()) {
            return;
        }

        LocalDateTime ultimaGeracao =
                dataUltimaGeracao.get();

        boolean existemNovasSolicitacoes =
                historicoDemandaRepository
                        .existeSolicitacaoApos(
                                ultimaGeracao
                        );

        if (existemNovasSolicitacoes) {
            return;
        }

        DateTimeFormatter formatador =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy 'às' HH:mm:ss"
                );

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Já existe uma previsão atualizada em "
                        + ultimaGeracao.format(formatador)
                        + " e não existem novas solicitações "
                        + "desde essa geração. Para executar "
                        + "novamente de forma intencional, "
                        + "envie forcarGeracao como true."
        );
    }
}