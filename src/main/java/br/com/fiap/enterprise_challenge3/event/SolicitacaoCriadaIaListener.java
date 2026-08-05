package br.com.fiap.enterprise_challenge3.event;

import br.com.fiap.enterprise_challenge3.service.AnaliseIaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SolicitacaoCriadaIaListener {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    SolicitacaoCriadaIaListener.class
            );

    private final AnaliseIaService analiseIaService;

    public SolicitacaoCriadaIaListener(
            AnaliseIaService analiseIaService
    ) {
        this.analiseIaService = analiseIaService;
    }

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void processarSolicitacaoCriada(
            SolicitacaoCriadaEvent event
    ) {
        try {
            logger.info(
                    "Iniciando análise automática da solicitação {}",
                    event.solicitacaoId()
            );

            analiseIaService.gerarAnaliseInicial(
                    event.solicitacaoId()
            );

            logger.info(
                    "Análise automática da solicitação {} concluída",
                    event.solicitacaoId()
            );

        } catch (Exception exception) {
            logger.error(
                    "Erro ao gerar análise automática da solicitação {}",
                    event.solicitacaoId(),
                    exception
            );
        }
    }
}