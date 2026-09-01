package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.AuditorCreateRequest;
import br.com.fiap.enterprise_challenge3.dto.ServidorResponse;
import br.com.fiap.enterprise_challenge3.model.Servidor;
import br.com.fiap.enterprise_challenge3.model.enums.PerfilServidor;
import br.com.fiap.enterprise_challenge3.repository.ServidorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

@Service
@Profile("local")
@Transactional
public class AuditorSetupService {

    private final ServidorRepository servidorRepository;
    private final PasswordEncoder passwordEncoder;
    private final String chaveConfigurada;

    public AuditorSetupService(
            ServidorRepository servidorRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.setup.auditor-key:}")
            String chaveConfigurada
    ) {
        this.servidorRepository = servidorRepository;
        this.passwordEncoder = passwordEncoder;
        this.chaveConfigurada = chaveConfigurada;
    }

    public ServidorResponse cadastrar(
            AuditorCreateRequest request,
            String chaveInformada
    ) {
        validarChave(
                chaveInformada
        );

        validarAuditorExistente();

        String matricula =
                request.matricula()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        String email =
                request.email()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        validarMatriculaDuplicada(
                matricula
        );

        validarEmailDuplicado(
                email
        );

        Servidor auditor =
                new Servidor(
                        request.nome().trim(),
                        matricula,
                        email,
                        passwordEncoder.encode(
                                request.senha()
                        ),
                        request.cargo().trim(),
                        PerfilServidor.AUDITOR
                );

        Servidor auditorSalvo =
                servidorRepository.save(
                        auditor
                );

        return ServidorResponse.fromEntity(
                auditorSalvo
        );
    }

    private void validarChave(
            String chaveInformada
    ) {
        if (
                chaveConfigurada == null
                        || chaveConfigurada.isBlank()
                        || chaveInformada == null
                        || chaveInformada.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Criação de auditor não autorizada"
            );
        }

        boolean chaveValida =
                MessageDigest.isEqual(
                        chaveConfigurada.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        chaveInformada.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        if (!chaveValida) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Criação de auditor não autorizada"
            );
        }
    }

    private void validarAuditorExistente() {
        boolean auditorAtivoExistente =
                servidorRepository
                        .existsByPerfilAndAtivoTrue(
                                PerfilServidor.AUDITOR
                        );

        if (auditorAtivoExistente) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um auditor ativo cadastrado"
            );
        }
    }

    private void validarMatriculaDuplicada(
            String matricula
    ) {
        if (
                servidorRepository
                        .existsByMatriculaIgnoreCase(
                                matricula
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A matrícula informada já está cadastrada"
            );
        }
    }

    private void validarEmailDuplicado(
            String email
    ) {
        if (
                servidorRepository
                        .existsByEmailIgnoreCase(
                                email
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O e-mail informado já está cadastrado"
            );
        }
    }
}