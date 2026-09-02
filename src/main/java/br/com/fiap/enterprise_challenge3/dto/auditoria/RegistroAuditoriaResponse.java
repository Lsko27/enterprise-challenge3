package br.com.fiap.enterprise_challenge3.dto.auditoria;

import br.com.fiap.enterprise_challenge3.model.RegistroAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;

import java.time.LocalDateTime;

public record RegistroAuditoriaResponse(
        Long id,
        TipoAtorAuditoria tipoAtor,
        Long atorId,
        AcaoAuditoria acao,
        TipoRecursoAuditoria tipoRecurso,
        String recursoId,
        String valorAnterior,
        String valorNovo,
        ResultadoAuditoria resultado,
        String metodoHttp,
        String endpoint,
        String enderecoIp,
        String detalhe,
        LocalDateTime dataEvento
) {

    public static RegistroAuditoriaResponse fromEntity(
            RegistroAuditoria registro
    ) {
        return new RegistroAuditoriaResponse(
                registro.getId(),
                registro.getTipoAtor(),
                registro.getAtorId(),
                registro.getAcao(),
                registro.getTipoRecurso(),
                registro.getRecursoId(),
                registro.getValorAnterior(),
                registro.getValorNovo(),
                registro.getResultado(),
                registro.getMetodoHttp(),
                registro.getEndpoint(),
                registro.getEnderecoIp(),
                registro.getDetalhe(),
                registro.getDataEvento()
        );
    }
}