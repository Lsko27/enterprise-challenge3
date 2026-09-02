package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.model.RegistroAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import br.com.fiap.enterprise_challenge3.repository.RegistroAuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroAuditoriaService {

    private final RegistroAuditoriaRepository auditoriaRepository;

    public RegistroAuditoriaService(
            RegistroAuditoriaRepository auditoriaRepository
    ) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void registrar(
            TipoAtorAuditoria tipoAtor,
            Long atorId,
            AcaoAuditoria acao,
            TipoRecursoAuditoria tipoRecurso,
            String recursoId,
            String valorAnterior,
            String valorNovo,
            ResultadoAuditoria resultado,
            ContextoAuditoria contexto,
            String detalhe
    ) {
        String metodoHttp = contexto == null
                ? null
                : contexto.metodoHttp();

        String endpoint = contexto == null
                ? null
                : contexto.endpoint();

        String enderecoIp = contexto == null
                ? null
                : contexto.enderecoIp();

        RegistroAuditoria registro =
                new RegistroAuditoria(
                        tipoAtor,
                        atorId,
                        acao,
                        tipoRecurso,
                        recursoId,
                        valorAnterior,
                        valorNovo,
                        resultado,
                        metodoHttp,
                        endpoint,
                        enderecoIp,
                        detalhe
                );

        auditoriaRepository.save(registro);
    }
}