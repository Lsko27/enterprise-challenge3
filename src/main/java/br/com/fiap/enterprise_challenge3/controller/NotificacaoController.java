package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.NotificacaoResponse;
import br.com.fiap.enterprise_challenge3.dto.NotificacoesMarcadasResponse;
import br.com.fiap.enterprise_challenge3.dto.QuantidadeNotificacoesResponse;
import br.com.fiap.enterprise_challenge3.service.NotificacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(
            NotificacaoService notificacaoService
    ) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoResponse>>
    listarMinhas(
            Authentication authentication
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                notificacaoService.listarMinhas(
                        cidadaoId
                )
        );
    }

    @GetMapping("/nao-lidas/quantidade")
    public ResponseEntity<QuantidadeNotificacoesResponse>
    contarNaoLidas(
            Authentication authentication
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                notificacaoService.contarNaoLidas(
                        cidadaoId
                )
        );
    }

    @PatchMapping("/{id}/ler")
    public ResponseEntity<NotificacaoResponse>
    marcarComoLida(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                notificacaoService.marcarComoLida(
                        id,
                        cidadaoId
                )
        );
    }

    @PatchMapping("/ler-todas")
    public ResponseEntity<NotificacoesMarcadasResponse>
    marcarTodasComoLidas(
            Authentication authentication
    ) {
        Long cidadaoId =
                extrairCidadaoId(authentication);

        return ResponseEntity.ok(
                notificacaoService.marcarTodasComoLidas(
                        cidadaoId
                )
        );
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