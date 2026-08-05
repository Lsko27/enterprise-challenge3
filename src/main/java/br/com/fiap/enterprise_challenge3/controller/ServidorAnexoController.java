package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.AnexoResponse;
import br.com.fiap.enterprise_challenge3.service.AnexoService;
import br.com.fiap.enterprise_challenge3.storage.ArquivoDownload;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping(
        "/api/servidor/solicitacoes/{solicitacaoId}/anexos"
)
public class ServidorAnexoController {

    private final AnexoService anexoService;

    public ServidorAnexoController(
            AnexoService anexoService
    ) {
        this.anexoService = anexoService;
    }

    @GetMapping
    public ResponseEntity<List<AnexoResponse>> listar(
            @PathVariable Long solicitacaoId
    ) {
        return ResponseEntity.ok(
                anexoService.listarParaServidor(
                        solicitacaoId
                )
        );
    }

    @GetMapping("/{anexoId}/arquivo")
    public ResponseEntity<Resource> baixar(
            @PathVariable Long solicitacaoId,
            @PathVariable Long anexoId
    ) {
        ArquivoDownload download =
                anexoService.baixarParaServidor(
                        solicitacaoId,
                        anexoId
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
}