package br.com.fiap.enterprise_challenge3.dto.auditoria;

import br.com.fiap.enterprise_challenge3.model.RegistroAuditoria;
import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaAuditoriaResponse(
        List<RegistroAuditoriaResponse> registros,
        int paginaAtual,
        int tamanhoPagina,
        long totalRegistros,
        int totalPaginas,
        boolean primeiraPagina,
        boolean ultimaPagina
) {

    public static PaginaAuditoriaResponse fromPage(
            Page<RegistroAuditoria> pagina
    ) {
        List<RegistroAuditoriaResponse> registros =
                pagina.getContent()
                        .stream()
                        .map(
                                RegistroAuditoriaResponse::fromEntity
                        )
                        .toList();

        return new PaginaAuditoriaResponse(
                registros,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast()
        );
    }
}