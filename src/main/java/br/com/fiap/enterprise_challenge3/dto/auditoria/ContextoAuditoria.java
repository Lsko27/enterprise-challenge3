package br.com.fiap.enterprise_challenge3.dto.auditoria;

import jakarta.servlet.http.HttpServletRequest;

public record ContextoAuditoria(
        String metodoHttp,
        String endpoint,
        String enderecoIp
) {

    public static ContextoAuditoria from(
            HttpServletRequest request
    ) {
        if (request == null) {
            return new ContextoAuditoria(
                    null,
                    null,
                    null
            );
        }

        return new ContextoAuditoria(
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr()
        );
    }
}