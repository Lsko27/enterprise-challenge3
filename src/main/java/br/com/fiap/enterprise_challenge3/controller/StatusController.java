package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.StatusResponse;
import br.com.fiap.enterprise_challenge3.service.StatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final StatusService statusService;

    public StatusController(
            StatusService statusService
    ) {
        this.statusService = statusService;
    }

    @GetMapping
    public ResponseEntity<StatusResponse>
    verificarStatus() {
        boolean bancoDisponivel =
                statusService
                        .bancoDadosDisponivel();

        StatusResponse response =
                new StatusResponse(
                        bancoDisponivel
                                ? "UP"
                                : "DEGRADED",
                        "GovAtende API",
                        bancoDisponivel
                                ? "UP"
                                : "DOWN",
                        Instant.now()
                );

        HttpStatus statusHttp =
                bancoDisponivel
                        ? HttpStatus.OK
                        : HttpStatus.SERVICE_UNAVAILABLE;

        return ResponseEntity
                .status(
                        statusHttp
                )
                .body(
                        response
                );
    }
}