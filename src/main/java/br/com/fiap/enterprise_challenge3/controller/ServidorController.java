package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.ServidorResponse;
import br.com.fiap.enterprise_challenge3.service.ServidorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/servidor")
public class ServidorController {

    private final ServidorService servidorService;

    public ServidorController(
            ServidorService servidorService
    ) {
        this.servidorService = servidorService;
    }

    @GetMapping("/me")
    public ResponseEntity<ServidorResponse>
    buscarMeuPerfil(
            Authentication authentication
    ) {
        Long servidorId =
                extrairServidorId(authentication);

        return ResponseEntity.ok(
                servidorService.buscarMeuPerfil(
                        servidorId
                )
        );
    }

    private Long extrairServidorId(
            Authentication authentication
    ) {
        if (authentication == null ||
                !authentication.isAuthenticated()) {

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