package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.dto.relatorio.RelatorioEstatisticoResponse;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import br.com.fiap.enterprise_challenge3.service.RegistroAuditoriaService;
import br.com.fiap.enterprise_challenge3.service.RelatorioEstatisticoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/servidor/relatorios")
public class RelatorioEstatisticoController {

    private final RelatorioEstatisticoService relatorioService;
    private final RegistroAuditoriaService auditoriaService;

    public RelatorioEstatisticoController(
            RelatorioEstatisticoService relatorioService,
            RegistroAuditoriaService auditoriaService
    ) {
        this.relatorioService = relatorioService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<RelatorioEstatisticoResponse>
    gerarRelatorio(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fim,

            Authentication authentication,

            HttpServletRequest httpRequest
    ) {
        Long usuarioId =
                extrairUsuarioId(authentication);

        TipoAtorAuditoria tipoAtor =
                extrairTipoAtor(authentication);

        RelatorioEstatisticoResponse relatorio =
                relatorioService.gerarRelatorio(
                        inicio,
                        fim
                );

        String periodo =
                relatorio.periodoInicio()
                        + "_"
                        + relatorio.periodoFim();

        auditoriaService.registrar(
                tipoAtor,
                usuarioId,
                AcaoAuditoria.CONSULTA_RELATORIO,
                TipoRecursoAuditoria.RELATORIO,
                periodo,
                null,
                null,
                ResultadoAuditoria.SUCESSO,
                ContextoAuditoria.from(httpRequest),
                "Relatório estatístico consultado; período="
                        + relatorio.periodoInicio()
                        + " até "
                        + relatorio.periodoFim()
        );

        return ResponseEntity.ok(relatorio);
    }

    private TipoAtorAuditoria extrairTipoAtor(
            Authentication authentication
    ) {
        boolean auditor = authentication
                .getAuthorities()
                .stream()
                .anyMatch(autoridade ->
                        "ROLE_AUDITOR".equals(
                                autoridade.getAuthority()
                        )
                );

        if (auditor) {
            return TipoAtorAuditoria.AUDITOR;
        }

        boolean servidor = authentication
                .getAuthorities()
                .stream()
                .anyMatch(autoridade ->
                        "ROLE_SERVIDOR".equals(
                                autoridade.getAuthority()
                        )
                );

        if (servidor) {
            return TipoAtorAuditoria.SERVIDOR;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Perfil sem permissão para consultar relatórios"
        );
    }

    private Long extrairUsuarioId(
            Authentication authentication
    ) {
        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário não autenticado"
            );
        }

        try {
            return Long.valueOf(
                    authentication.getName()
            );

        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Identificação do usuário inválida"
            );
        }
    }
}