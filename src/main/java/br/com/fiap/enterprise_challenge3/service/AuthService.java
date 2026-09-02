package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.LoginRequest;
import br.com.fiap.enterprise_challenge3.dto.LoginResponse;
import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.model.Cidadao;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
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
    private final RegistroAuditoriaService auditoriaService;

    public AuthService(
            CidadaoRepository cidadaoRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RegistroAuditoriaService auditoriaService
    ) {
        this.cidadaoRepository = cidadaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditoriaService = auditoriaService;
    }

    public LoginResponse login(
            LoginRequest request,
            ContextoAuditoria contextoAuditoria
    ) {
        String cpf =
                normalizarCpf(request.cpf());

        Cidadao cidadao =
                cidadaoRepository
                        .findByCpf(cpf)
                        .orElse(null);

        if (cidadao == null) {
            registrarLogin(
                    TipoAtorAuditoria.SISTEMA,
                    null,
                    null,
                    ResultadoAuditoria.NEGADO,
                    contextoAuditoria,
                    "Falha no login de cidadão: credenciais inválidas"
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "CPF ou senha inválidos"
            );
        }

        if (!Boolean.TRUE.equals(cidadao.getAtivo())) {
            registrarLogin(
                    TipoAtorAuditoria.CIDADAO,
                    cidadao.getId(),
                    cidadao.getId().toString(),
                    ResultadoAuditoria.NEGADO,
                    contextoAuditoria,
                    "Falha no login de cidadão: conta inativa"
            );

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuário desativado"
            );
        }

        boolean senhaCorreta =
                passwordEncoder.matches(
                        request.senha(),
                        cidadao.getSenha()
                );

        if (!senhaCorreta) {
            registrarLogin(
                    TipoAtorAuditoria.CIDADAO,
                    cidadao.getId(),
                    cidadao.getId().toString(),
                    ResultadoAuditoria.NEGADO,
                    contextoAuditoria,
                    "Falha no login de cidadão: credenciais inválidas"
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "CPF ou senha inválidos"
            );
        }

        String token =
                jwtService.gerarToken(cidadao);

        registrarLogin(
                TipoAtorAuditoria.CIDADAO,
                cidadao.getId(),
                cidadao.getId().toString(),
                ResultadoAuditoria.SUCESSO,
                contextoAuditoria,
                "Login de cidadão realizado com sucesso"
        );

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiracaoEmSegundos(),
                cidadao.getId(),
                cidadao.getNome(),
                "Login realizado com sucesso"
        );
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
                TipoRecursoAuditoria.CIDADAO,
                recursoId,
                null,
                null,
                resultado,
                contextoAuditoria,
                detalhe
        );
    }

    private String normalizarCpf(
            String cpf
    ) {
        return cpf.replaceAll(
                "\\D",
                ""
        );
    }
}