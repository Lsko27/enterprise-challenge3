package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.CidadaoCreateRequest;
import br.com.fiap.enterprise_challenge3.dto.CidadaoResponse;
import br.com.fiap.enterprise_challenge3.dto.CidadaoUpdateRequest;
import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.service.CidadaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cidadaos")
public class CidadaoController {

    private final CidadaoService cidadaoService;

    public CidadaoController(
            CidadaoService cidadaoService
    ) {
        this.cidadaoService = cidadaoService;
    }

    @PostMapping
    public ResponseEntity<CidadaoResponse> cadastrar(
            @Valid @RequestBody
            CidadaoCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        cidadaoService.cadastrar(request)
                );
    }

    @GetMapping("/me")
    public ResponseEntity<CidadaoResponse> buscarMeuPerfil(
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                cidadaoService.buscarPorId(
                        cidadaoId,
                        ContextoAuditoria.from(httpRequest)
                )
        );
    }

    @PutMapping("/me")
    public ResponseEntity<CidadaoResponse> atualizarMeuPerfil(
            Authentication authentication,

            @Valid @RequestBody
            CidadaoUpdateRequest request,

            HttpServletRequest httpRequest
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                cidadaoService.atualizar(
                        cidadaoId,
                        request,
                        ContextoAuditoria.from(httpRequest)
                )
        );
    }

    @PatchMapping("/me/desativar")
    public ResponseEntity<Void> desativarMeuPerfil(
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        cidadaoService.desativar(
                cidadaoId,
                ContextoAuditoria.from(httpRequest)
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    private Long extrairCidadaoId(
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