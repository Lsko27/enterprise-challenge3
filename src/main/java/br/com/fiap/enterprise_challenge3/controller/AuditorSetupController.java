package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.AuditorCreateRequest;
import br.com.fiap.enterprise_challenge3.dto.ServidorResponse;
import br.com.fiap.enterprise_challenge3.service.AuditorSetupService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
@RequestMapping("/api/setup/auditor")
public class AuditorSetupController {

    private final AuditorSetupService auditorSetupService;

    public AuditorSetupController(
            AuditorSetupService auditorSetupService
    ) {
        this.auditorSetupService =
                auditorSetupService;
    }

    @PostMapping
    public ResponseEntity<ServidorResponse> cadastrar(
            @RequestHeader(
                    name = "X-Setup-Key",
                    required = false
            )
            String chaveConfiguracao,

            @Valid
            @RequestBody
            AuditorCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        auditorSetupService.cadastrar(
                                request,
                                chaveConfiguracao
                        )
                );
    }
}