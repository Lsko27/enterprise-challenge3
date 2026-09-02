package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.ServidorLoginRequest;
import br.com.fiap.enterprise_challenge3.dto.ServidorLoginResponse;
import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.model.Servidor;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.PerfilServidor;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
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
    private final RegistroAuditoriaService auditoriaService;

    public ServidorAuthService(
            ServidorRepository servidorRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RegistroAuditoriaService auditoriaService
    ) {
        this.servidorRepository = servidorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditoriaService = auditoriaService;
    }

    public ServidorLoginResponse login(
            ServidorLoginRequest request,
            ContextoAuditoria contextoAuditoria
    ) {
        String matricula =
                request.matricula().trim();

        Servidor servidor =
                servidorRepository
                        .findByMatriculaIgnoreCase(
                                matricula
                        )
                        .orElse(null);

        if (servidor == null) {
            registrarLogin(
                    TipoAtorAuditoria.SISTEMA,
                    null,
                    null,
                    ResultadoAuditoria.NEGADO,
                    contextoAuditoria,
                    "Falha no login interno: credenciais inválidas"
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Matrícula ou senha inválidas"
            );
        }

        TipoAtorAuditoria tipoAtor =
                identificarTipoAtor(servidor);

        if (!Boolean.TRUE.equals(servidor.getAtivo())) {
            registrarLogin(
                    tipoAtor,
                    servidor.getId(),
                    servidor.getId().toString(),
                    ResultadoAuditoria.NEGADO,
                    contextoAuditoria,
                    "Falha no login interno: conta inativa"
            );

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
            registrarLogin(
                    tipoAtor,
                    servidor.getId(),
                    servidor.getId().toString(),
                    ResultadoAuditoria.NEGADO,
                    contextoAuditoria,
                    "Falha no login interno: credenciais inválidas"
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Matrícula ou senha inválidas"
            );
        }

        String token =
                jwtService.gerarToken(servidor);

        registrarLogin(
                tipoAtor,
                servidor.getId(),
                servidor.getId().toString(),
                ResultadoAuditoria.SUCESSO,
                contextoAuditoria,
                tipoAtor == TipoAtorAuditoria.AUDITOR
                        ? "Login de auditor realizado com sucesso"
                        : "Login de servidor realizado com sucesso"
        );

        return new ServidorLoginResponse(
                token,
                "Bearer",
                jwtService.getExpiracaoEmSegundos(),
                servidor.getId(),
                servidor.getNome(),
                servidor.getCargo(),
                servidor.getPerfil(),
                "Login realizado com sucesso"
        );
    }

    private TipoAtorAuditoria identificarTipoAtor(
            Servidor servidor
    ) {
        if (
                PerfilServidor.AUDITOR.equals(
                        servidor.getPerfil()
                )
        ) {
            return TipoAtorAuditoria.AUDITOR;
        }

        return TipoAtorAuditoria.SERVIDOR;
    }

    private void registrarLogin(
            TipoAtorAuditoria tipoAtor,
            Long atorId,
            String recursoId,
            ResultadoAuditoria resultado,
            ContextoAuditoria contextoAuditoria,
            String detalhe
    ) {
        auditoriaService.registrar(
                tipoAtor,
                atorId,
                AcaoAuditoria.LOGIN,
                TipoRecursoAuditoria.SERVIDOR,
                recursoId,
                null,
                null,
                resultado,
                contextoAuditoria,
                detalhe
        );
    }
}