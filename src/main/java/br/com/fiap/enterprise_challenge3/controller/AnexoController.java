package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.AnexoResponse;
import br.com.fiap.enterprise_challenge3.service.AnexoService;
import br.com.fiap.enterprise_challenge3.storage.ArquivoDownload;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping(
        "/api/solicitacoes/{solicitacaoId}/anexos"
)
public class AnexoController {

    private final AnexoService anexoService;

    public AnexoController(
            AnexoService anexoService
    ) {
        this.anexoService = anexoService;
    }

    @PostMapping(
            consumes =
                    MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<AnexoResponse> adicionar(
            @PathVariable Long solicitacaoId,
            @RequestParam("arquivo")
            MultipartFile arquivo,
            Authentication authentication
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        anexoService.adicionarDoCidadao(
                                solicitacaoId,
                                cidadaoId,
                                arquivo
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<AnexoResponse>> listar(
            @PathVariable Long solicitacaoId,
            Authentication authentication
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                anexoService.listarDoCidadao(
                        solicitacaoId,
                        cidadaoId
                )
        );
    }

    @GetMapping("/{anexoId}/arquivo")
    public ResponseEntity<Resource> baixar(
            @PathVariable Long solicitacaoId,
            @PathVariable Long anexoId,
            Authentication authentication
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        ArquivoDownload download =
                anexoService.baixarDoCidadao(
                        solicitacaoId,
                        anexoId,
                        cidadaoId
                );

        return criarRespostaArquivo(download);
    }

    private ResponseEntity<Resource> criarRespostaArquivo(
            ArquivoDownload download
    ) {
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

    private Long extrairCidadaoId(
            Authentication authentication
    ) {
        if (authentication == null ||
                !authentication.isAuthenticated()) {

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