package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.dto.auditoria.PaginaAuditoriaResponse;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import br.com.fiap.enterprise_challenge3.service.ConsultaAuditoriaService;
import br.com.fiap.enterprise_challenge3.service.RegistroAuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/servidor/auditoria")
public class AuditoriaController {

    private final ConsultaAuditoriaService consultaService;
    private final RegistroAuditoriaService registroService;

    public AuditoriaController(
            ConsultaAuditoriaService consultaService,
            RegistroAuditoriaService registroService
    ) {
        this.consultaService = consultaService;
        this.registroService = registroService;
    }

    @GetMapping
    public ResponseEntity<PaginaAuditoriaResponse> consultar(
            @RequestParam(required = false)
            TipoAtorAuditoria tipoAtor,

            @RequestParam(required = false)
            Long atorId,

            @RequestParam(required = false)
            AcaoAuditoria acao,

            @RequestParam(required = false)
            TipoRecursoAuditoria tipoRecurso,

            @RequestParam(required = false)
            String recursoId,

            @RequestParam(required = false)
            ResultadoAuditoria resultado,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fim,

            @RequestParam(defaultValue = "0")
            int pagina,

            @RequestParam(defaultValue = "20")
            int tamanho,

            Authentication authentication,

            HttpServletRequest httpRequest
    ) {
        Long auditorId =
                extrairAuditorId(authentication);

        PaginaAuditoriaResponse resposta =
                consultaService.consultar(
                        tipoAtor,
                        atorId,
                        acao,
                        tipoRecurso,
                        recursoId,
                        resultado,
                        inicio,
                        fim,
                        pagina,
                        tamanho
                );

        registroService.registrar(
                TipoAtorAuditoria.AUDITOR,
                auditorId,
                AcaoAuditoria.CONSULTA_AUDITORIA,
                TipoRecursoAuditoria.AUDITORIA,
                null,
                null,
                null,
                ResultadoAuditoria.SUCESSO,
                ContextoAuditoria.from(httpRequest),
                montarDetalheConsulta(
                        tipoAtor,
                        atorId,
                        acao,
                        tipoRecurso,
                        recursoId,
                        resultado,
                        inicio,
                        fim,
                        pagina,
                        tamanho
                )
        );

        return ResponseEntity.ok(resposta);
    }

    private String montarDetalheConsulta(
            TipoAtorAuditoria tipoAtor,
            Long atorId,
            AcaoAuditoria acao,
            TipoRecursoAuditoria tipoRecurso,
            String recursoId,
            ResultadoAuditoria resultado,
            LocalDate inicio,
            LocalDate fim,
            int pagina,
            int tamanho
    ) {
        return "Consulta paginada da trilha de auditoria"
                + "; tipoAtor=" + valorOuTodos(tipoAtor)
                + "; atorId=" + valorOuTodos(atorId)
                + "; acao=" + valorOuTodos(acao)
                + "; tipoRecurso=" + valorOuTodos(tipoRecurso)
                + "; recursoId=" + valorOuTodos(recursoId)
                + "; resultado=" + valorOuTodos(resultado)
                + "; inicio=" + valorOuTodos(inicio)
                + "; fim=" + valorOuTodos(fim)
                + "; pagina=" + pagina
                + "; tamanho=" + tamanho;
    }

    private String valorOuTodos(
            Object valor
    ) {
        if (valor == null) {
            return "TODOS";
        }

        String texto = valor.toString().trim();

        return texto.isEmpty()
                ? "TODOS"
                : texto;
    }

    private Long extrairAuditorId(
            Authentication authentication
    ) {
        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Auditor não autenticado"
            );
        }

        try {
            return Long.valueOf(
                    authentication.getName()
            );

        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Identificação do auditor inválida"
            );
        }
    }
}