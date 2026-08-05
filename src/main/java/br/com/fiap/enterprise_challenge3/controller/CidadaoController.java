package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.CidadaoCreateRequest;
import br.com.fiap.enterprise_challenge3.dto.CidadaoResponse;
import br.com.fiap.enterprise_challenge3.dto.CidadaoUpdateRequest;
import br.com.fiap.enterprise_challenge3.service.CidadaoService;
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

    /*
     * Cadastro público.
     * Ainda não existe usuário autenticado nesse momento.
     */
    @PostMapping
    public ResponseEntity<CidadaoResponse> cadastrar(
            @Valid @RequestBody CidadaoCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cidadaoService.cadastrar(request));
    }

    /*
     * Retorna somente o cidadão identificado pelo JWT.
     */
    @GetMapping("/me")
    public ResponseEntity<CidadaoResponse> buscarMeuPerfil(
            Authentication authentication
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                cidadaoService.buscarPorId(cidadaoId)
        );
    }

    /*
     * Atualiza somente o cidadão identificado pelo JWT.
     */
    @PutMapping("/me")
    public ResponseEntity<CidadaoResponse> atualizarMeuPerfil(
            Authentication authentication,
            @Valid @RequestBody CidadaoUpdateRequest request
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                cidadaoService.atualizar(
                        cidadaoId,
                        request
                )
        );
    }

    /*
     * Desativa somente o cidadão identificado pelo JWT.
     */
    @PatchMapping("/me/desativar")
    public ResponseEntity<Void> desativarMeuPerfil(
            Authentication authentication
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        cidadaoService.desativar(cidadaoId);

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