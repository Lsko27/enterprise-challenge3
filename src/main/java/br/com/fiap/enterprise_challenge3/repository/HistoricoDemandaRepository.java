package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.HistoricoDemandaIaItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class HistoricoDemandaRepository {

    private final JdbcTemplate jdbcTemplate;

    public HistoricoDemandaRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existeSolicitacaoApos(
            LocalDateTime dataGeracao
    ) {
        String sql = """
            SELECT COUNT(*)
            FROM T_ETP_SOLICITACAO
            WHERE DT_ABERTURA > ?
            """;

        Integer quantidade =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,
                        Timestamp.valueOf(
                                dataGeracao
                        )
                );

        return quantidade != null
                && quantidade > 0;
    }

    public List<HistoricoDemandaIaItem> buscarHistorico(
            int periodoHistoricoDias
    ) {
        String sql = """
                SELECT
                    s.ID_SOLICITACAO,
                    INITCAP(TRIM(e.NM_BAIRRO)) AS NM_BAIRRO,
                    c.ID_CATEGORIA,
                    c.NM_CATEGORIA,
                    ss.ID_SUBSERVICO,
                    ss.NM_SUBSERVICO,
                    s.DT_ABERTURA
                FROM T_ETP_SOLICITACAO s
                INNER JOIN T_ETP_ENDERECO e
                    ON e.ID_ENDERECO = s.ID_ENDERECO
                INNER JOIN T_ETP_SUBSERVICO ss
                    ON ss.ID_SUBSERVICO = s.ID_SUBSERVICO
                INNER JOIN T_ETP_CATEGORIA c
                    ON c.ID_CATEGORIA = ss.ID_CATEGORIA
                WHERE s.DT_ABERTURA >=
                    SYSTIMESTAMP
                    - NUMTODSINTERVAL(?, 'DAY')
                AND s.DT_ABERTURA <= SYSTIMESTAMP
                ORDER BY s.DT_ABERTURA
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> {
                    Timestamp dataAbertura =
                            resultSet.getTimestamp(
                                    "DT_ABERTURA"
                            );

                    return new HistoricoDemandaIaItem(
                            resultSet.getLong(
                                    "ID_SOLICITACAO"
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
                            dataAbertura.toLocalDateTime()
                    );
                },
                periodoHistoricoDias
        );
    }
}