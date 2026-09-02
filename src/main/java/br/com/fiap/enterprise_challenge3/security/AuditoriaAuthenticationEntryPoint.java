package br.com.fiap.enterprise_challenge3.security;

import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import br.com.fiap.enterprise_challenge3.service.RegistroAuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuditoriaAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    AuditoriaAuthenticationEntryPoint.class
            );

    private final RegistroAuditoriaService auditoriaService;

    public AuditoriaAuthenticationEntryPoint(
            RegistroAuditoriaService auditoriaService
    ) {
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        try {
            auditoriaService.registrar(
                    TipoAtorAuditoria.SISTEMA,
                    null,
                    AcaoAuditoria.ACESSO_NEGADO,
                    TipoRecursoAuditoria.API,
                    request.getRequestURI(),
                    null,
                    null,
                    ResultadoAuditoria.NEGADO,
                    ContextoAuditoria.from(request),
                    "Requisição bloqueada: autenticação ausente ou inválida"
            );

        } catch (RuntimeException erroAuditoria) {
            LOGGER.error(
                    "Não foi possível registrar a tentativa não autenticada",
                    erroAuditoria
            );
        }

        response.sendError(
                HttpStatus.UNAUTHORIZED.value(),
                "Autenticação necessária"
        );
    }
}