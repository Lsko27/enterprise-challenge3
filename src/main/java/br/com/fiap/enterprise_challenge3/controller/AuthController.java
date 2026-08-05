package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.LoginRequest;
import br.com.fiap.enterprise_challenge3.dto.LoginResponse;
import br.com.fiap.enterprise_challenge3.dto.ServidorLoginRequest;
import br.com.fiap.enterprise_challenge3.dto.ServidorLoginResponse;
import br.com.fiap.enterprise_challenge3.service.AuthService;
import br.com.fiap.enterprise_challenge3.service.ServidorAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ServidorAuthService servidorAuthService;

    public AuthController(
            AuthService authService,
            ServidorAuthService servidorAuthService
    ) {
        this.authService = authService;
        this.servidorAuthService =
                servidorAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginCidadao(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/servidor/login")
    public ResponseEntity<ServidorLoginResponse>
    loginServidor(
            @Valid @RequestBody
            ServidorLoginRequest request
    ) {
        return ResponseEntity.ok(
                servidorAuthService.login(request)
        );
    }
}