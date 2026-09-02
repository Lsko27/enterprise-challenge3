package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.relatorio.DistribuicaoEstatisticaResponse;
import br.com.fiap.enterprise_challenge3.dto.relatorio.IndicadoresEstatisticosResponse;
import br.com.fiap.enterprise_challenge3.dto.relatorio.RelatorioEstatisticoResponse;
import br.com.fiap.enterprise_challenge3.dto.relatorio.SerieTemporalDiariaResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RelatorioCsvService {

    public byte[] gerar(
            RelatorioEstatisticoResponse relatorio
    ) {
        StringBuilder csv =
                new StringBuilder();

        /*
         * BOM para o Excel reconhecer corretamente UTF-8.
         */
        csv.append('\uFEFF');

        adicionarLinha(
                csv,
                "RELATÓRIO ESTATÍSTICO GOVATENDE"
        );

        adicionarLinha(
                csv,
                "Período inicial",
                relatorio.periodoInicio()
        );

        adicionarLinha(
                csv,
                "Período final",
                relatorio.periodoFim()
        );

        adicionarLinhaVazia(csv);

        adicionarIndicadores(
                csv,
                relatorio.indicadores()
        );

        adicionarDistribuicao(
                csv,
                "DISTRIBUIÇÃO POR CATEGORIA",
                relatorio.porCategoria()
        );

        adicionarDistribuicao(
                csv,
                "DISTRIBUIÇÃO POR STATUS",
                relatorio.porStatus()
        );

        adicionarDistribuicao(
                csv,
                "DISTRIBUIÇÃO POR URGÊNCIA",
                relatorio.porUrgencia()
        );

        adicionarEvolucaoDiaria(
                csv,
                relatorio.evolucaoDiaria()
        );

        adicionarAnalises(
                csv,
                relatorio.analisesESugestoes()
        );

        return csv
                .toString()
                .getBytes(StandardCharsets.UTF_8);
    }

    private void adicionarIndicadores(
            StringBuilder csv,
            IndicadoresEstatisticosResponse indicadores
    ) {
        adicionarLinha(
                csv,
                "INDICADORES"
        );

        adicionarLinha(
                csv,
                "Indicador",
                "Valor"
        );

        adicionarLinha(
                csv,
                "Total de solicitações",
                indicadores.totalSolicitacoes()
        );

        adicionarLinha(
                csv,
                "Solicitações em aberto",
                indicadores.solicitacoesEmAberto()
        );

        adicionarLinha(
                csv,
                "Solicitações concluídas",
                indicadores.solicitacoesConcluidas()
        );

        adicionarLinha(
                csv,
                "Solicitações canceladas",
                indicadores.solicitacoesCanceladas()
        );

        adicionarLinha(
                csv,
                "Taxa de conclusão",
                indicadores.taxaConclusaoPercentual()
                        + "%"
        );

        adicionarLinha(
                csv,
                "Percentual de urgência alta ou crítica",
                indicadores.percentualAltaOuCritica()
                        + "%"
        );

        adicionarLinha(
                csv,
                "Média diária",
                indicadores.mediaDiaria()
        );

        adicionarLinha(
                csv,
                "Mediana diária",
                indicadores.medianaDiaria()
        );

        String modas = indicadores
                .modasDiarias()
                .stream()
                .map(String::valueOf)
                .collect(
                        Collectors.joining(", ")
                );

        adicionarLinha(
                csv,
                "Moda diária",
                modas
        );

        adicionarLinha(
                csv,
                "Variância populacional",
                indicadores.varianciaPopulacional()
        );

        adicionarLinha(
                csv,
                "Desvio-padrão populacional",
                indicadores.desvioPadraoPopulacional()
        );

        adicionarLinha(
                csv,
                "Coeficiente de variação",
                indicadores
                        .coeficienteVariacaoPercentual()
                        + "%"
        );

        adicionarLinha(
                csv,
                "Tempo médio de resolução em horas",
                indicadores.tempoMedioResolucaoHoras()
        );

        adicionarLinhaVazia(csv);
    }

    private void adicionarDistribuicao(
            StringBuilder csv,
            String titulo,
            List<DistribuicaoEstatisticaResponse> itens
    ) {
        adicionarLinha(
                csv,
                titulo
        );

        adicionarLinha(
                csv,
                "Código",
                "Descrição",
                "Quantidade",
                "Percentual"
        );

        for (
                DistribuicaoEstatisticaResponse item
                : itens
        ) {
            adicionarLinha(
                    csv,
                    item.codigo(),
                    item.descricao(),
                    item.quantidade(),
                    item.percentual() + "%"
            );
        }

        adicionarLinhaVazia(csv);
    }

    private void adicionarEvolucaoDiaria(
            StringBuilder csv,
            List<SerieTemporalDiariaResponse> evolucao
    ) {
        adicionarLinha(
                csv,
                "EVOLUÇÃO DIÁRIA"
        );

        adicionarLinha(
                csv,
                "Data",
                "Quantidade"
        );

        for (
                SerieTemporalDiariaResponse item
                : evolucao
        ) {
            adicionarLinha(
                    csv,
                    item.data(),
                    item.quantidade()
            );
        }

        adicionarLinhaVazia(csv);
    }

    private void adicionarAnalises(
            StringBuilder csv,
            List<String> analises
    ) {
        adicionarLinha(
                csv,
                "ANÁLISES E SUGESTÕES"
        );

        adicionarLinha(
                csv,
                "Número",
                "Análise"
        );

        for (
                int indice = 0;
                indice < analises.size();
                indice++
        ) {
            adicionarLinha(
                    csv,
                    indice + 1,
                    analises.get(indice)
            );
        }
    }

    private void adicionarLinha(
            StringBuilder csv,
            Object... valores
    ) {
        for (
                int indice = 0;
                indice < valores.length;
                indice++
        ) {
            if (indice > 0) {
                csv.append(';');
            }

            csv.append(
                    escaparCelula(
                            valores[indice]
                    )
            );
        }

        csv.append("\r\n");
    }

    private void adicionarLinhaVazia(
            StringBuilder csv
    ) {
        csv.append("\r\n");
    }

    private String escaparCelula(
            Object valor
    ) {
        String texto =
                valor == null
                        ? ""
                        : valor.toString();

        /*
         * Evita CSV Injection quando o arquivo é aberto
         * em programas de planilha.
         */
        String textoSemEspacoInicial =
                texto.stripLeading();

        if (
                !textoSemEspacoInicial.isEmpty()
                        && "=+-@".indexOf(
                        textoSemEspacoInicial.charAt(0)
                ) >= 0
        ) {
            texto = "'" + texto;
        }

        return "\""
                + texto.replace(
                "\"",
                "\"\""
        )
                + "\"";
    }
}