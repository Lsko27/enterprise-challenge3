package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.relatorio.DistribuicaoEstatisticaResponse;
import br.com.fiap.enterprise_challenge3.dto.relatorio.IndicadoresEstatisticosResponse;
import br.com.fiap.enterprise_challenge3.dto.relatorio.RelatorioEstatisticoResponse;
import br.com.fiap.enterprise_challenge3.dto.relatorio.SerieTemporalDiariaResponse;
import br.com.fiap.enterprise_challenge3.repository.RelatorioEstatisticoRepository;
import br.com.fiap.enterprise_challenge3.repository.RelatorioEstatisticoRepository.ContagemAgrupada;
import br.com.fiap.enterprise_challenge3.repository.RelatorioEstatisticoRepository.ContagemDiaria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class RelatorioEstatisticoService {

    private static final int PERIODO_PADRAO_DIAS = 30;
    private static final int PERIODO_MAXIMO_DIAS = 366;
    private static final int ESCALA_DECIMAL = 2;

    private final RelatorioEstatisticoRepository relatorioRepository;

    public RelatorioEstatisticoService(
            RelatorioEstatisticoRepository relatorioRepository
    ) {
        this.relatorioRepository = relatorioRepository;
    }

    public RelatorioEstatisticoResponse gerarRelatorio(
            LocalDate inicio,
            LocalDate fim
    ) {
        LocalDate periodoFim = fim == null
                ? LocalDate.now()
                : fim;

        LocalDate periodoInicio = inicio == null
                ? periodoFim.minusDays(
                PERIODO_PADRAO_DIAS - 1L
        )
                : inicio;

        int quantidadeDias = validarPeriodo(
                periodoInicio,
                periodoFim
        );

        LocalDateTime inicioInclusivo =
                periodoInicio.atStartOfDay();

        LocalDateTime fimExclusivo =
                periodoFim.plusDays(1).atStartOfDay();

        List<SerieTemporalDiariaResponse> evolucaoDiaria =
                montarSerieTemporal(
                        periodoInicio,
                        quantidadeDias,
                        relatorioRepository.contarPorDia(
                                inicioInclusivo,
                                fimExclusivo
                        )
                );

        long totalSolicitacoes = evolucaoDiaria
                .stream()
                .mapToLong(
                        SerieTemporalDiariaResponse::quantidade
                )
                .sum();

        List<DistribuicaoEstatisticaResponse> porCategoria =
                converterDistribuicao(
                        relatorioRepository.contarPorCategoria(
                                inicioInclusivo,
                                fimExclusivo
                        ),
                        totalSolicitacoes
                );

        List<DistribuicaoEstatisticaResponse> porStatus =
                converterDistribuicao(
                        relatorioRepository.contarPorStatus(
                                inicioInclusivo,
                                fimExclusivo
                        ),
                        totalSolicitacoes
                );

        List<DistribuicaoEstatisticaResponse> porUrgencia =
                converterDistribuicao(
                        relatorioRepository.contarPorUrgencia(
                                inicioInclusivo,
                                fimExclusivo
                        ),
                        totalSolicitacoes
                );

        long concluidas = quantidadePorCodigo(
                porStatus,
                "CONCLUIDA"
        );

        long canceladas = quantidadePorCodigo(
                porStatus,
                "CANCELADA"
        );

        long emAberto = Math.max(
                0,
                totalSolicitacoes
                        - concluidas
                        - canceladas
        );

        long altaOuCritica = quantidadePorCodigo(
                porUrgencia,
                "ALTA"
        ) + quantidadePorCodigo(
                porUrgencia,
                "CRITICA"
        );

        List<Long> quantidadesDiarias =
                evolucaoDiaria
                        .stream()
                        .map(
                                SerieTemporalDiariaResponse::quantidade
                        )
                        .toList();

        double mediaSemArredondamento =
                calcularMedia(
                        quantidadesDiarias
                );

        BigDecimal mediaDiaria =
                arredondar(
                        mediaSemArredondamento
                );

        BigDecimal medianaDiaria = arredondar(
                calcularMediana(
                        quantidadesDiarias
                )
        );

        List<Long> modasDiarias = calcularModas(
                quantidadesDiarias
        );

        double variancia =
                calcularVarianciaPopulacional(
                        quantidadesDiarias
                );

        BigDecimal varianciaPopulacional =
                arredondar(
                        variancia
                );

        double desvioSemArredondamento =
                Math.sqrt(
                        variancia
                );

        BigDecimal desvioPadraoPopulacional =
                arredondar(
                        desvioSemArredondamento
                );

        BigDecimal coeficienteVariacao =
                calcularCoeficienteVariacao(
                        mediaSemArredondamento,
                        desvioSemArredondamento
                );

        BigDecimal tempoMedioResolucao =
                relatorioRepository
                        .calcularTempoMedioResolucaoHoras(
                                inicioInclusivo,
                                fimExclusivo
                        )
                        .setScale(
                                ESCALA_DECIMAL,
                                RoundingMode.HALF_UP
                        );

        IndicadoresEstatisticosResponse indicadores =
                new IndicadoresEstatisticosResponse(
                        totalSolicitacoes,
                        emAberto,
                        concluidas,
                        canceladas,
                        calcularPercentual(
                                concluidas,
                                totalSolicitacoes
                        ),
                        calcularPercentual(
                                altaOuCritica,
                                totalSolicitacoes
                        ),
                        mediaDiaria,
                        medianaDiaria,
                        modasDiarias,
                        varianciaPopulacional,
                        desvioPadraoPopulacional,
                        coeficienteVariacao,
                        tempoMedioResolucao
                );

        List<String> analisesESugestoes =
                gerarAnalises(
                        indicadores,
                        porCategoria
                );

        return new RelatorioEstatisticoResponse(
                periodoInicio,
                periodoFim,
                indicadores,
                porCategoria,
                porStatus,
                porUrgencia,
                evolucaoDiaria,
                analisesESugestoes
        );
    }

    private int validarPeriodo(
            LocalDate inicio,
            LocalDate fim
    ) {
        if (inicio.isAfter(fim)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data inicial não pode ser posterior à data final"
            );
        }

        long quantidadeDias =
                ChronoUnit.DAYS.between(
                        inicio,
                        fim
                ) + 1;

        if (quantidadeDias > PERIODO_MAXIMO_DIAS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O relatório aceita períodos de até 366 dias"
            );
        }

        return Math.toIntExact(
                quantidadeDias
        );
    }

    private List<SerieTemporalDiariaResponse>
    montarSerieTemporal(
            LocalDate periodoInicio,
            int quantidadeDias,
            List<ContagemDiaria> contagens
    ) {
        Map<LocalDate, Long> quantidadePorData =
                new HashMap<>();

        for (ContagemDiaria contagem : contagens) {
            quantidadePorData.put(
                    contagem.data(),
                    contagem.quantidade()
            );
        }

        List<SerieTemporalDiariaResponse> serie =
                new ArrayList<>(
                        quantidadeDias
                );

        for (
                int indice = 0;
                indice < quantidadeDias;
                indice++
        ) {
            LocalDate data =
                    periodoInicio.plusDays(
                            indice
                    );

            serie.add(
                    new SerieTemporalDiariaResponse(
                            data,
                            quantidadePorData.getOrDefault(
                                    data,
                                    0L
                            )
                    )
            );
        }

        return List.copyOf(
                serie
        );
    }

    private List<DistribuicaoEstatisticaResponse>
    converterDistribuicao(
            List<ContagemAgrupada> contagens,
            long totalSolicitacoes
    ) {
        return contagens
                .stream()
                .map(contagem ->
                        new DistribuicaoEstatisticaResponse(
                                contagem.codigo(),
                                humanizarDescricao(
                                        contagem.descricao()
                                ),
                                contagem.quantidade(),
                                calcularPercentual(
                                        contagem.quantidade(),
                                        totalSolicitacoes
                                )
                        )
                )
                .toList();
    }

    private long quantidadePorCodigo(
            List<DistribuicaoEstatisticaResponse> distribuicao,
            String codigo
    ) {
        return distribuicao
                .stream()
                .filter(item ->
                        codigo.equals(
                                item.codigo()
                        )
                )
                .mapToLong(
                        DistribuicaoEstatisticaResponse::quantidade
                )
                .findFirst()
                .orElse(
                        0L
                );
    }

    private double calcularMedia(
            List<Long> valores
    ) {
        return valores
                .stream()
                .mapToLong(
                        Long::longValue
                )
                .average()
                .orElse(
                        0.0
                );
    }

    private double calcularMediana(
            List<Long> valores
    ) {
        if (valores.isEmpty()) {
            return 0.0;
        }

        List<Long> valoresOrdenados =
                valores
                        .stream()
                        .sorted()
                        .toList();

        int tamanho =
                valoresOrdenados.size();

        int meio =
                tamanho / 2;

        if (tamanho % 2 != 0) {
            return valoresOrdenados.get(
                    meio
            );
        }

        return (
                valoresOrdenados.get(
                        meio - 1
                )
                        + valoresOrdenados.get(
                        meio
                )
        ) / 2.0;
    }

    private List<Long> calcularModas(
            List<Long> valores
    ) {
        if (valores.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> frequencias =
                new HashMap<>();

        for (Long valor : valores) {
            frequencias.merge(
                    valor,
                    1L,
                    Long::sum
            );
        }

        long maiorFrequencia =
                frequencias
                        .values()
                        .stream()
                        .mapToLong(
                                Long::longValue
                        )
                        .max()
                        .orElse(
                                0L
                        );

        if (maiorFrequencia <= 1) {
            return List.of();
        }

        return frequencias
                .entrySet()
                .stream()
                .filter(entrada ->
                        entrada.getValue()
                                == maiorFrequencia
                )
                .map(
                        Map.Entry::getKey
                )
                .sorted()
                .toList();
    }

    private double calcularVarianciaPopulacional(
            List<Long> valores
    ) {
        if (valores.isEmpty()) {
            return 0.0;
        }

        double media =
                calcularMedia(
                        valores
                );

        double somaDosQuadrados =
                valores
                        .stream()
                        .mapToDouble(valor -> {
                            double diferenca =
                                    valor - media;

                            return diferenca
                                    * diferenca;
                        })
                        .sum();

        return somaDosQuadrados
                / valores.size();
    }

    private BigDecimal calcularCoeficienteVariacao(
            double media,
            double desvioPadrao
    ) {
        if (media == 0.0) {
            return BigDecimal.ZERO.setScale(
                    ESCALA_DECIMAL,
                    RoundingMode.HALF_UP
            );
        }

        return arredondar(
                (desvioPadrao / media) * 100
        );
    }

    private BigDecimal calcularPercentual(
            long quantidade,
            long total
    ) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(
                    ESCALA_DECIMAL,
                    RoundingMode.HALF_UP
            );
        }

        return BigDecimal
                .valueOf(
                        quantidade
                )
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .divide(
                        BigDecimal.valueOf(total),
                        ESCALA_DECIMAL,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal arredondar(
            double valor
    ) {
        if (!Double.isFinite(valor)) {
            return BigDecimal.ZERO.setScale(
                    ESCALA_DECIMAL,
                    RoundingMode.HALF_UP
            );
        }

        return BigDecimal
                .valueOf(
                        valor
                )
                .setScale(
                        ESCALA_DECIMAL,
                        RoundingMode.HALF_UP
                );
    }

    private String humanizarDescricao(
            String descricao
    ) {
        return switch (descricao) {
            case "REGISTRADA" ->
                    "Registrada";

            case "EM_TRIAGEM" ->
                    "Em triagem";

            case "EM_ANDAMENTO" ->
                    "Em andamento";

            case "AGUARDANDO_INFORMACOES" ->
                    "Aguardando informações";

            case "CONCLUIDA" ->
                    "Concluída";

            case "CANCELADA" ->
                    "Cancelada";

            case "BAIXA" ->
                    "Baixa";

            case "MEDIA" ->
                    "Média";

            case "ALTA" ->
                    "Alta";

            case "CRITICA" ->
                    "Crítica";

            default ->
                    descricao;
        };
    }

    private List<String> gerarAnalises(
            IndicadoresEstatisticosResponse indicadores,
            List<DistribuicaoEstatisticaResponse> porCategoria
    ) {
        if (
                indicadores.totalSolicitacoes() == 0
        ) {
            return List.of(
                    "Não há solicitações no período selecionado. "
                            + "Amplie o intervalo para obter uma análise."
            );
        }

        List<String> analises =
                new ArrayList<>();

        if (!porCategoria.isEmpty()) {
            DistribuicaoEstatisticaResponse principal =
                    porCategoria.getFirst();

            analises.add(
                    "A categoria "
                            + principal.descricao()
                            + " concentrou "
                            + principal.percentual()
                            + "% das solicitações. "
                            + "Priorize capacidade operacional "
                            + "nessa frente."
            );
        }

        if (
                indicadores
                        .taxaConclusaoPercentual()
                        .compareTo(
                                BigDecimal.valueOf(60)
                        ) < 0
        ) {
            analises.add(
                    "A taxa de conclusão ficou abaixo de 60%. "
                            + "Revise o estoque de solicitações abertas "
                            + "e os gargalos do fluxo de atendimento."
            );
        } else {
            analises.add(
                    "A taxa de conclusão foi de "
                            + indicadores
                            .taxaConclusaoPercentual()
                            + "%. Mantenha o acompanhamento "
                            + "por categoria para evitar "
                            + "acúmulo localizado."
            );
        }

        if (
                indicadores
                        .percentualAltaOuCritica()
                        .compareTo(
                                BigDecimal.valueOf(30)
                        ) >= 0
        ) {
            analises.add(
                    "Solicitações de urgência alta ou crítica "
                            + "representaram "
                            + indicadores
                            .percentualAltaOuCritica()
                            + "%. Considere uma equipe "
                            + "de resposta rápida."
            );
        }

        if (
                indicadores
                        .coeficienteVariacaoPercentual()
                        .compareTo(
                                BigDecimal.valueOf(50)
                        ) > 0
        ) {
            analises.add(
                    "A demanda diária apresentou alta variabilidade "
                            + "(coeficiente de variação de "
                            + indicadores
                            .coeficienteVariacaoPercentual()
                            + "%). Use escalas flexíveis "
                            + "de atendimento."
            );
        } else {
            analises.add(
                    "A demanda diária apresentou variabilidade "
                            + "controlada, com coeficiente de variação "
                            + "de "
                            + indicadores
                            .coeficienteVariacaoPercentual()
                            + "%."
            );
        }

        return List.copyOf(
                analises
        );
    }
}