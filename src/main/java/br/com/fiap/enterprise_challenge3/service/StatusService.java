package br.com.fiap.enterprise_challenge3.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class StatusService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    StatusService.class
            );

    private static final int
            TIMEOUT_VALIDACAO_SEGUNDOS = 2;

    private final DataSource dataSource;

    public StatusService(
            DataSource dataSource
    ) {
        this.dataSource = dataSource;
    }

    public boolean bancoDadosDisponivel() {
        try (
                Connection connection =
                        dataSource.getConnection()
        ) {
            return connection.isValid(
                    TIMEOUT_VALIDACAO_SEGUNDOS
            );

        } catch (SQLException exception) {
            /*
             * O detalhe técnico fica somente no log.
             * A resposta pública não revela endereço,
             * usuário ou informações do Oracle.
             */
            LOGGER.warn(
                    "Falha na verificação de disponibilidade do banco de dados: {}",
                    exception.getMessage()
            );

            return false;
        }
    }
}