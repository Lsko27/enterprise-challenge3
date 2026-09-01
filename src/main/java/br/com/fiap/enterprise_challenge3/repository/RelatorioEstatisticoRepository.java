package br.com.fiap.enterprise_challenge3.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class RelatorioEstatisticoRepository {

    private final JdbcTemplate jdbcTemplate;

    public RelatorioEstatisticoRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ContagemDiaria> contarPorDia(
            LocalDateTime inicio,
            LocalDateTime fimExclusivo
    ) {
        String sql = """
                SELECT
                    TRUNC(CAST(S.DT_ABERTURA AS DATE))
                        AS DATA_REFERENCIA,
                    COUNT(*) AS QUANTIDADE
                FROM T_ETP_SOLICITACAO S
                WHERE S.DT_ABERTURA >= ?
                  AND S.DT_ABERTURA < ?
                GROUP BY
                    TRUNC(CAST(S.DT_ABERTURA AS DATE))
                ORDER BY DATA_REFERENCIA
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> {
                    Date data = resultSet.getDate(
                            "DATA_REFERENCIA"
                    );

                    return new ContagemDiaria(
                            data.toLocalDate(),
                            resultSet.getLong(
                                    "QUANTIDADE"
                            )
                    );
                },
                Timestamp.valueOf(inicio),
                Timestamp.valueOf(fimExclusivo)
        );
    }

    public List<ContagemAgrupada> contarPorCategoria(
            LocalDateTime inicio,
            LocalDateTime fimExclusivo
    ) {
        String sql = """
                SELECT
                    TO_CHAR(C.ID_CATEGORIA) AS CODIGO,
                    C.NM_CATEGORIA AS DESCRICAO,
                    COUNT(*) AS QUANTIDADE
                FROM T_ETP_SOLICITACAO S
                INNER JOIN T_ETP_SUBSERVICO SS
                    ON SS.ID_SUBSERVICO = S.ID_SUBSERVICO
                INNER JOIN T_ETP_CATEGORIA C
                    ON C.ID_CATEGORIA = SS.ID_CATEGORIA
                WHERE S.DT_ABERTURA >= ?
                  AND S.DT_ABERTURA < ?
                GROUP BY
                    C.ID_CATEGORIA,
                    C.NM_CATEGORIA
                ORDER BY
                    QUANTIDADE DESC,
                    C.NM_CATEGORIA
                """;

        return consultarDistribuicao(
                sql,
                inicio,
                fimExclusivo
        );
    }

    public List<ContagemAgrupada> contarPorStatus(
            LocalDateTime inicio,
            LocalDateTime fimExclusivo
    ) {
        String sql = """
                SELECT
                    S.ST_SOLICITACAO AS CODIGO,
                    S.ST_SOLICITACAO AS DESCRICAO,
                    COUNT(*) AS QUANTIDADE
                FROM T_ETP_SOLICITACAO S
                WHERE S.DT_ABERTURA >= ?
                  AND S.DT_ABERTURA < ?
                GROUP BY S.ST_SOLICITACAO
                ORDER BY
                    QUANTIDADE DESC,
                    S.ST_SOLICITACAO
                """;

        return consultarDistribuicao(
                sql,
                inicio,
                fimExclusivo
        );
    }

    public List<ContagemAgrupada> contarPorUrgencia(
            LocalDateTime inicio,
            LocalDateTime fimExclusivo
    ) {
        String sql = """
                SELECT
                    S.NV_URGENCIA AS CODIGO,
                    S.NV_URGENCIA AS DESCRICAO,
                    COUNT(*) AS QUANTIDADE
                FROM T_ETP_SOLICITACAO S
                WHERE S.DT_ABERTURA >= ?
                  AND S.DT_ABERTURA < ?
                GROUP BY S.NV_URGENCIA
                ORDER BY
                    QUANTIDADE DESC,
                    S.NV_URGENCIA
                """;

        return consultarDistribuicao(
                sql,
                inicio,
                fimExclusivo
        );
    }

    public BigDecimal calcularTempoMedioResolucaoHoras(
            LocalDateTime inicio,
            LocalDateTime fimExclusivo
    ) {
        String sql = """
                SELECT AVG(
                    (
                        CAST(S.DT_ATUALIZACAO AS DATE)
                        - CAST(S.DT_ABERTURA AS DATE)
                    ) * 24
                ) AS MEDIA_HORAS
                FROM T_ETP_SOLICITACAO S
                WHERE S.ST_SOLICITACAO = 'CONCLUIDA'
                  AND S.DT_ABERTURA >= ?
                  AND S.DT_ABERTURA < ?
                """;

        BigDecimal resultado =
                jdbcTemplate.queryForObject(
                        sql,
                        BigDecimal.class,
                        Timestamp.valueOf(inicio),
                        Timestamp.valueOf(fimExclusivo)
                );

        return resultado == null
                ? BigDecimal.ZERO
                : resultado;
    }

    private List<ContagemAgrupada> consultarDistribuicao(
            String sql,
            LocalDateTime inicio,
            LocalDateTime fimExclusivo
    ) {
        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) ->
                        new ContagemAgrupada(
                                resultSet.getString(
                                        "CODIGO"
                                ),
                                resultSet.getString(
                                        "DESCRICAO"
                                ),
                                resultSet.getLong(
                                        "QUANTIDADE"
                                )
                        ),
                Timestamp.valueOf(inicio),
                Timestamp.valueOf(fimExclusivo)
        );
    }

    public record ContagemDiaria(
            LocalDate data,
            long quantidade
    ) {
    }

    public record ContagemAgrupada(
            String codigo,
            String descricao,
            long quantidade
    ) {
    }
}