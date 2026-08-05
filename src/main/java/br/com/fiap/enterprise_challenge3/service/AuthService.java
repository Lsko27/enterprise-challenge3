package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.LoginRequest;
import br.com.fiap.enterprise_challenge3.dto.LoginResponse;
import br.com.fiap.enterprise_challenge3.model.Cidadao;
import br.com.fiap.enterprise_challenge3.repository.CidadaoRepository;
import br.com.fiap.enterprise_challenge3.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final CidadaoRepository cidadaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            CidadaoRepository cidadaoRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.cidadaoRepository = cidadaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        String cpf = normalizarCpf(request.cpf());

        Cidadao cidadao = cidadaoRepository
                .findByCpf(cpf)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "CPF ou senha inválidos"
                        )
                );

        if (!Boolean.TRUE.equals(cidadao.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuário desativado"
            );
        }

        boolean senhaCorreta = passwordEncoder.matches(
                request.senha(),
                cidadao.getSenha()
        );

        if (!senhaCorreta) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "CPF ou senha inválidos"
            );
        }

        String token = jwtService.gerarToken(cidadao);

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiracaoEmSegundos(),
                cidadao.getId(),
                cidadao.getNome(),
                "Login realizado com sucesso"
        );
    }

    private String normalizarCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }
}