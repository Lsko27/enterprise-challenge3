package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.dto.relatorio.RelatorioEstatisticoResponse;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import br.com.fiap.enterprise_challenge3.service.RegistroAuditoriaService;
import br.com.fiap.enterprise_challenge3.service.RelatorioCsvService;
import br.com.fiap.enterprise_challenge3.service.RelatorioEstatisticoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/servidor/relatorios")
public class RelatorioEstatisticoController {

    private final RelatorioEstatisticoService relatorioService;
    private final RelatorioCsvService csvService;
    private final RegistroAuditoriaService auditoriaService;

    public RelatorioEstatisticoController(
            RelatorioEstatisticoService relatorioService,
            RelatorioCsvService csvService,
            RegistroAuditoriaService auditoriaService
    ) {
        this.relatorioService = relatorioService;
        this.csvService = csvService;
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

        registrarAuditoria(
                tipoAtor,
                usuarioId,
                AcaoAuditoria.CONSULTA_RELATORIO,
                relatorio,
                ContextoAuditoria.from(httpRequest),
                "Relatório estatístico consultado"
        );

        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/estatisticas/exportar")
    public ResponseEntity<byte[]> exportarRelatorio(
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

        byte[] arquivo =
                csvService.gerar(relatorio);

        registrarAuditoria(
                tipoAtor,
                usuarioId,
                AcaoAuditoria.EXPORTACAO_RELATORIO,
                relatorio,
                ContextoAuditoria.from(httpRequest),
                "Relatório estatístico exportado em CSV"
        );

        String nomeArquivo =
                "relatorio-estatistico-"
                        + relatorio.periodoInicio()
                        + "-a-"
                        + relatorio.periodoFim()
                        + ".csv";

        ContentDisposition disposicao =
                ContentDisposition
                        .attachment()
                        .filename(
                                nomeArquivo,
                                StandardCharsets.UTF_8
                        )
                        .build();

        MediaType tipoCsv =
                MediaType.parseMediaType(
                        "text/csv;charset=UTF-8"
                );

        return ResponseEntity.ok()
                .contentType(tipoCsv)
                .contentLength(arquivo.length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposicao.toString()
                )
                .body(arquivo);
    }

    private void registrarAuditoria(
            TipoAtorAuditoria tipoAtor,
            Long usuarioId,
            AcaoAuditoria acao,
            RelatorioEstatisticoResponse relatorio,
            ContextoAuditoria contexto,
            String descricao
    ) {
        String periodo =
                relatorio.periodoInicio()
                        + "_"
                        + relatorio.periodoFim();

        auditoriaService.registrar(
                tipoAtor,
                usuarioId,
                acao,
                TipoRecursoAuditoria.RELATORIO,
                periodo,
                null,
                null,
                ResultadoAuditoria.SUCESSO,
                contexto,
                descricao
                        + "; período="
                        + relatorio.periodoInicio()
                        + " até "
                        + relatorio.periodoFim()
        );
    }

    private TipoAtorAuditoria extrairTipoAtor(
            Authentication authentication
    ) {
        boolean auditor =
                authentication
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

        boolean servidor =
                authentication
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