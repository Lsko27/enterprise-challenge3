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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuditoriaAccessDeniedHandler
        implements AccessDeniedHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    AuditoriaAccessDeniedHandler.class
            );

    private final RegistroAuditoriaService auditoriaService;

    public AuditoriaAccessDeniedHandler(
            RegistroAuditoriaService auditoriaService
    ) {
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException exception
    ) throws IOException, ServletException {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        TipoAtorAuditoria tipoAtor =
                extrairTipoAtor(authentication);

        Long atorId =
                extrairAtorId(authentication);

        try {
            auditoriaService.registrar(
                    tipoAtor,
                    atorId,
                    AcaoAuditoria.ACESSO_NEGADO,
                    TipoRecursoAuditoria.API,
                    request.getRequestURI(),
                    null,
                    null,
                    ResultadoAuditoria.NEGADO,
                    ContextoAuditoria.from(request),
                    "Requisição bloqueada: perfil sem permissão para acessar o recurso"
            );

        } catch (RuntimeException erroAuditoria) {
            LOGGER.error(
                    "Não foi possível registrar o acesso negado",
                    erroAuditoria
            );
        }

        response.sendError(
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado"
        );
    }

    private TipoAtorAuditoria extrairTipoAtor(
            Authentication authentication
    ) {
        if (
                possuiAutoridade(
                        authentication,
                        "ROLE_AUDITOR"
                )
        ) {
            return TipoAtorAuditoria.AUDITOR;
        }

        if (
                possuiAutoridade(
                        authentication,
                        "ROLE_SERVIDOR"
                )
        ) {
            return TipoAtorAuditoria.SERVIDOR;
        }

        if (
                possuiAutoridade(
                        authentication,
                        "ROLE_CIDADAO"
                )
        ) {
            return TipoAtorAuditoria.CIDADAO;
        }

        return TipoAtorAuditoria.SISTEMA;
    }

    private boolean possuiAutoridade(
            Authentication authentication,
            String autoridadeEsperada
    ) {
        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {
            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(autoridade ->
                        autoridadeEsperada.equals(
                                autoridade.getAuthority()
                        )
                );
    }

    private Long extrairAtorId(
            Authentication authentication
    ) {
        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {
            return null;
        }

        try {
            return Long.valueOf(
                    authentication.getName()
            );

        } catch (NumberFormatException exception) {
            return null;
        }
    }
}