package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.ServidorLoginRequest;
import br.com.fiap.enterprise_challenge3.dto.ServidorLoginResponse;
import br.com.fiap.enterprise_challenge3.model.Servidor;
import br.com.fiap.enterprise_challenge3.repository.ServidorRepository;
import br.com.fiap.enterprise_challenge3.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ServidorAuthService {

    private final ServidorRepository servidorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ServidorAuthService(
            ServidorRepository servidorRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.servidorRepository = servidorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public ServidorLoginResponse login(
            ServidorLoginRequest request
    ) {
        String matricula =
                request.matricula().trim();

        Servidor servidor = servidorRepository
                .findByMatriculaIgnoreCase(matricula)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Matrícula ou senha inválidas"
                        )
                );

        if (!Boolean.TRUE.equals(servidor.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Servidor desativado"
            );
        }

        boolean senhaCorreta =
                passwordEncoder.matches(
                        request.senha(),
                        servidor.getSenha()
                );

        if (!senhaCorreta) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Matrícula ou senha inválidas"
            );
        }

        String token =
                jwtService.gerarToken(servidor);

        return new ServidorLoginResponse(
                token,
                "Bearer",
                jwtService.getExpiracaoEmSegundos(),
                servidor.getId(),
                servidor.getNome(),
                servidor.getCargo(),
                "Login realizado com sucesso"
        );
    }
}