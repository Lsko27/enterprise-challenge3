package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.AnexoResponse;
import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import br.com.fiap.enterprise_challenge3.service.AnexoService;
import br.com.fiap.enterprise_challenge3.service.RegistroAuditoriaService;
import br.com.fiap.enterprise_challenge3.storage.ArquivoDownload;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping(
        "/api/servidor/solicitacoes/{solicitacaoId}/anexos"
)
public class ServidorAnexoController {

    private final AnexoService anexoService;
    private final RegistroAuditoriaService auditoriaService;

    public ServidorAnexoController(
            AnexoService anexoService,
            RegistroAuditoriaService auditoriaService
    ) {
        this.anexoService = anexoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<List<AnexoResponse>> listar(
            @PathVariable Long solicitacaoId,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Long servidorId =
                extrairServidorId(authentication);

        List<AnexoResponse> anexos =
                anexoService.listarParaServidor(
                        solicitacaoId
                );

        auditoriaService.registrar(
                TipoAtorAuditoria.SERVIDOR,
                servidorId,
                AcaoAuditoria.CONSULTA_DADOS_PESSOAIS,
                TipoRecursoAuditoria.ANEXO,
                "SOLICITACAO_" + solicitacaoId,
                null,
                null,
                ResultadoAuditoria.SUCESSO,
                ContextoAuditoria.from(httpRequest),
                "Servidor consultou os anexos de uma solicitação"
        );

        return ResponseEntity.ok(anexos);
    }

    @GetMapping("/{anexoId}/arquivo")
    public ResponseEntity<Resource> baixar(
            @PathVariable Long solicitacaoId,
            @PathVariable Long anexoId,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Long servidorId =
                extrairServidorId(authentication);

        ArquivoDownload download =
                anexoService.baixarParaServidor(
                        solicitacaoId,
                        anexoId
                );

        auditoriaService.registrar(
                TipoAtorAuditoria.SERVIDOR,
                servidorId,
                AcaoAuditoria.DOWNLOAD_ANEXO,
                TipoRecursoAuditoria.ANEXO,
                anexoId.toString(),
                null,
                null,
                ResultadoAuditoria.SUCESSO,
                ContextoAuditoria.from(httpRequest),
                "Servidor acessou o arquivo de um anexo"
        );

        MediaType tipo;

        try {
            tipo = MediaType.parseMediaType(
                    download.tipoConteudo()
            );

        } catch (IllegalArgumentException exception) {
            tipo = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition disposicao =
                ContentDisposition
                        .inline()
                        .filename(
                                download.nomeOriginal(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(tipo)
                .contentLength(download.tamanho())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposicao.toString()
                )
                .body(download.recurso());
    }

    private Long extrairServidorId(
            Authentication authentication
    ) {
        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Servidor não autenticado"
            );
        }

        try {
            return Long.valueOf(
                    authentication.getName()
            );

        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Identificação do servidor inválida"
            );
        }
    }
}