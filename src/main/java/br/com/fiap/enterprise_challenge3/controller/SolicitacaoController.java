package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.*;
import br.com.fiap.enterprise_challenge3.service.SolicitacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(
            SolicitacaoService solicitacaoService
    ) {
        this.solicitacaoService = solicitacaoService;
    }

    @PostMapping
    public ResponseEntity<SolicitacaoResponse> cadastrar(
            Authentication authentication,
            @Valid @RequestBody SolicitacaoCreateRequest request
    ) {
        Long cidadaoId = extrairCidadaoId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        solicitacaoService.cadastrar(
                                cidadaoId,
                                request
                        )
                );
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<SolicitacaoResponse>> listarMinhas(
            Authentication authentication
    ) {
        Long cidadaoId = extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                solicitacaoService.listarMinhas(cidadaoId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoResponse> buscarMinhaPorId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long cidadaoId = extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                solicitacaoService.buscarMinhaPorId(
                        id,
                        cidadaoId
                )
        );
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoSolicitacaoResponse>> listarHistorico(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long cidadaoId = extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                solicitacaoService.listarHistorico(
                        id,
                        cidadaoId
                )
        );
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long cidadaoId = extrairCidadaoId(authentication);

        solicitacaoService.cancelar(
                id,
                cidadaoId
        );

        return ResponseEntity.noContent().build();
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
            return Long.valueOf(authentication.getName());

        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Identificação do usuário inválida"
            );
        }
    }
}