package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.PrevisaoDemandaConsultaResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.HistoricoPrevisaoDemandaResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class ConsultaPrevisaoDemandaRepository {

    private final JdbcTemplate jdbcTemplate;

    public ConsultaPrevisaoDemandaRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<LocalDateTime>
    buscarDataUltimaGeracao() {

        String sql = """
            SELECT MAX(DT_PREVISAO)
            FROM T_ETP_PREVISAO_DEMANDA
            """;

        Timestamp dataUltimaGeracao =
                jdbcTemplate.queryForObject(
                        sql,
                        Timestamp.class
                );

        if (dataUltimaGeracao == null) {
            return Optional.empty();
        }

        return Optional.of(
                dataUltimaGeracao.toLocalDateTime()
        );
    }

    public List<PrevisaoDemandaConsultaResponse>
    buscarUltimaGeracao() {

        String sql = """
                SELECT
                    P.ID_PREVISAO,
                    P.NM_BAIRRO,

                    P.ID_CATEGORIA,
                    C.NM_CATEGORIA,

                    P.ID_SUBSERVICO,
                    SS.NM_SUBSERVICO,

                    P.NR_PERIODO_HISTORICO_DIAS,
                    P.NR_PERIODO_PREVISAO_DIAS,

                    P.NR_OCORRENCIAS_HISTORICAS,
                    P.NR_MEDIA_DIARIA,
                    P.NR_TENDENCIA_PERCENTUAL,
                    P.NR_QUANTIDADE_PREVISTA,

                    P.DS_TENDENCIA,
                    P.DS_NIVEL_DEMANDA,

                    P.NR_CONFIANCA,
                    P.DS_JUSTIFICATIVA,

                    P.NM_MODELO,
                    P.DS_VERSAO_MODELO,
                    P.DT_PREVISAO

                FROM T_ETP_PREVISAO_DEMANDA P

                INNER JOIN T_ETP_CATEGORIA C
                    ON C.ID_CATEGORIA = P.ID_CATEGORIA

                INNER JOIN T_ETP_SUBSERVICO SS
                    ON SS.ID_SUBSERVICO = P.ID_SUBSERVICO

                WHERE P.DT_PREVISAO = (
                    SELECT MAX(P2.DT_PREVISAO)
                    FROM T_ETP_PREVISAO_DEMANDA P2
                )

                ORDER BY
                    P.NR_QUANTIDADE_PREVISTA DESC,
                    P.NR_CONFIANCA DESC,
                    P.ID_PREVISAO
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> {

                    Timestamp dataPrevisao =
                            resultSet.getTimestamp(
                                    "DT_PREVISAO"
                            );

                    return new PrevisaoDemandaConsultaResponse(
                            resultSet.getLong(
                                    "ID_PREVISAO"
                            ),

                            resultSet.getString(
                                    "NM_BAIRRO"
                            ),

                            resultSet.getLong(
                                    "ID_CATEGORIA"
                            ),

                            resultSet.getString(
                                    "NM_CATEGORIA"
                            ),

                            resultSet.getLong(
                                    "ID_SUBSERVICO"
                            ),

                            resultSet.getString(
                                    "NM_SUBSERVICO"
                            ),

                            resultSet.getInt(
                                    "NR_PERIODO_HISTORICO_DIAS"
                            ),

                            resultSet.getInt(
                                    "NR_PERIODO_PREVISAO_DIAS"
                            ),

                            resultSet.getInt(
                                    "NR_OCORRENCIAS_HISTORICAS"
                            ),

                            resultSet.getBigDecimal(
                                    "NR_MEDIA_DIARIA"
                            ),

                            resultSet.getBigDecimal(
                                    "NR_TENDENCIA_PERCENTUAL"
                            ),

                            resultSet.getBigDecimal(
                                    "NR_QUANTIDADE_PREVISTA"
                            ),

                            resultSet.getString(
                                    "DS_TENDENCIA"
                            ),

                            resultSet.getString(
                                    "DS_NIVEL_DEMANDA"
                            ),

                            resultSet.getBigDecimal(
                                    "NR_CONFIANCA"
                            ),

                            resultSet.getString(
                                    "DS_JUSTIFICATIVA"
                            ),

                            resultSet.getString(
                                    "NM_MODELO"
                            ),

                            resultSet.getString(
                                    "DS_VERSAO_MODELO"
                            ),

                            dataPrevisao.toLocalDateTime()
                    );
                }
        );
    }

    public List<HistoricoPrevisaoDemandaResponse>
    buscarHistoricoGeracoes() {

        String sql = """
            SELECT
                P.DT_PREVISAO,

                MAX(
                    P.NR_PERIODO_HISTORICO_DIAS
                ) AS NR_PERIODO_HISTORICO_DIAS,

                MAX(
                    P.NR_PERIODO_PREVISAO_DIAS
                ) AS NR_PERIODO_PREVISAO_DIAS,

                COUNT(*) AS TOTAL_PREVISOES,

                SUM(
                    P.NR_OCORRENCIAS_HISTORICAS
                ) AS TOTAL_OCORRENCIAS_HISTORICAS,

                SUM(
                    P.NR_QUANTIDADE_PREVISTA
                ) AS QUANTIDADE_TOTAL_PREVISTA,

                SUM(
                    CASE
                        WHEN P.DS_NIVEL_DEMANDA = 'CRITICA'
                        THEN 1
                        ELSE 0
                    END
                ) AS DEMANDAS_CRITICAS,

                SUM(
                    CASE
                        WHEN P.DS_NIVEL_DEMANDA = 'ALTA'
                        THEN 1
                        ELSE 0
                    END
                ) AS DEMANDAS_ALTAS,

                SUM(
                    CASE
                        WHEN P.DS_NIVEL_DEMANDA = 'MEDIA'
                        THEN 1
                        ELSE 0
                    END
                ) AS DEMANDAS_MEDIAS,

                SUM(
                    CASE
                        WHEN P.DS_NIVEL_DEMANDA = 'BAIXA'
                        THEN 1
                        ELSE 0
                    END
                ) AS DEMANDAS_BAIXAS,

                MAX(
                    P.NR_CONFIANCA
                ) AS MAIOR_CONFIANCA,

                MAX(
                    P.NM_MODELO
                ) AS NM_MODELO,

                MAX(
                    P.DS_VERSAO_MODELO
                ) AS DS_VERSAO_MODELO

            FROM T_ETP_PREVISAO_DEMANDA P

            GROUP BY
                P.DT_PREVISAO

            ORDER BY
                P.DT_PREVISAO DESC
            """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> {

                    Timestamp dataPrevisao =
                            resultSet.getTimestamp(
                                    "DT_PREVISAO"
                            );

                    return new HistoricoPrevisaoDemandaResponse(
                            dataPrevisao.toLocalDateTime(),

                            resultSet.getInt(
                                    "NR_PERIODO_HISTORICO_DIAS"
                            ),

                            resultSet.getInt(
                                    "NR_PERIODO_PREVISAO_DIAS"
                            ),

                            resultSet.getInt(
                                    "TOTAL_PREVISOES"
                            ),

                            resultSet.getInt(
                                    "TOTAL_OCORRENCIAS_HISTORICAS"
                            ),

                            resultSet.getBigDecimal(
                                    "QUANTIDADE_TOTAL_PREVISTA"
                            ),

                            resultSet.getLong(
                                    "DEMANDAS_CRITICAS"
                            ),

                            resultSet.getLong(
                                    "DEMANDAS_ALTAS"
                            ),

                            resultSet.getLong(
                                    "DEMANDAS_MEDIAS"
                            ),

                            resultSet.getLong(
                                    "DEMANDAS_BAIXAS"
                            ),

                            resultSet.getBigDecimal(
                                    "MAIOR_CONFIANCA"
                            ),

                            resultSet.getString(
                                    "NM_MODELO"
                            ),

                            resultSet.getString(
                                    "DS_VERSAO_MODELO"
                            )
                    );
                }
        );
    }
}