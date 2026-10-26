package br.com.fiap.enterprise_challenge3.security;

import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import br.com.fiap.enterprise_challenge3.service.RegistroAuditoriaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LoginRateLimitFilter
        extends OncePerRequestFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    LoginRateLimitFilter.class
            );

    private static final Set<String> ROTAS_LOGIN =
            Set.of(
                    "/api/auth/login",
                    "/api/auth/servidor/login"
            );

    private static final Set<Integer>
            STATUS_QUE_CONTAM_COMO_FALHA =
            Set.of(
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.FORBIDDEN.value()
            );

    private final ConcurrentHashMap<String, EstadoTentativas>
            tentativasPorChave =
            new ConcurrentHashMap<>();

    private final AtomicLong totalRequisicoes =
            new AtomicLong();

    private final RegistroAuditoriaService auditoriaService;
    private final int maximoFalhas;
    private final long janelaSegundos;

    public LoginRateLimitFilter(
            RegistroAuditoriaService auditoriaService,

            @Value(
                    "${app.security.login-rate-limit.max-failures:5}"
            )
            int maximoFalhas,

            @Value(
                    "${app.security.login-rate-limit.window-seconds:300}"
            )
            long janelaSegundos
    ) {
        if (maximoFalhas < 1) {
            throw new IllegalArgumentException(
                    "O limite de falhas deve ser maior que zero"
            );
        }

        if (janelaSegundos < 1) {
            throw new IllegalArgumentException(
                    "A janela do rate limiting deve ser maior que zero"
            );
        }

        this.auditoriaService = auditoriaService;
        this.maximoFalhas = maximoFalhas;
        this.janelaSegundos = janelaSegundos;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        return !HttpMethod.POST.matches(
                request.getMethod()
        ) || !ROTAS_LOGIN.contains(
                request.getRequestURI()
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Instant agora = Instant.now();

        executarLimpezaPeriodica(
                agora
        );

        String chave =
                criarChave(
                        request
                );

        EstadoTentativas estado =
                obterEstadoValido(
                        chave,
                        agora
                );

        if (
                estado != null
                        && estado.bloqueadoAte() != null
                        && agora.isBefore(
                        estado.bloqueadoAte()
                )
        ) {
            responderBloqueio(
                    request,
                    response,
                    estado.bloqueadoAte(),
                    agora
            );

            return;
        }

        filterChain.doFilter(
                request,
                response
        );

        int status =
                response.getStatus();

        if (
                status >= 200
                        && status < 300
        ) {
            tentativasPorChave.remove(
                    chave
            );

            return;
        }

        if (
                STATUS_QUE_CONTAM_COMO_FALHA.contains(
                        status
                )
        ) {
            boolean bloqueioAtivado =
                    registrarFalha(
                            chave,
                            agora
                    );

            if (bloqueioAtivado) {
                registrarBloqueioNaAuditoria(
                        request
                );
            }
        }
    }

    private String criarChave(
            HttpServletRequest request
    ) {
        return request.getRemoteAddr()
                + ":"
                + request.getRequestURI();
    }

    private EstadoTentativas obterEstadoValido(
            String chave,
            Instant agora
    ) {
        EstadoTentativas estado =
                tentativasPorChave.get(
                        chave
                );

        if (estado == null) {
            return null;
        }

        if (
                estado.bloqueadoAte() != null
                        && !agora.isBefore(
                        estado.bloqueadoAte()
                )
        ) {
            tentativasPorChave.remove(
                    chave,
                    estado
            );

            return null;
        }

        Instant fimDaJanela =
                estado.inicioDaJanela()
                        .plusSeconds(
                                janelaSegundos
                        );

        if (
                estado.bloqueadoAte() == null
                        && !agora.isBefore(
                        fimDaJanela
                )
        ) {
            tentativasPorChave.remove(
                    chave,
                    estado
            );

            return null;
        }

        return estado;
    }

    private boolean registrarFalha(
            String chave,
            Instant agora
    ) {
        AtomicBoolean bloqueioAtivado =
                new AtomicBoolean(
                        false
                );

        tentativasPorChave.compute(
                chave,
                (chaveAtual, estadoAtual) -> {
                    EstadoTentativas estadoValido =
                            validarEstadoParaAtualizacao(
                                    estadoAtual,
                                    agora
                            );

                    int novasFalhas =
                            estadoValido == null
                                    ? 1
                                    : estadoValido.falhas() + 1;

                    Instant inicioDaJanela =
                            estadoValido == null
                                    ? agora
                                    : estadoValido.inicioDaJanela();

                    Instant bloqueadoAte =
                            null;

                    if (
                            novasFalhas
                                    >= maximoFalhas
                    ) {
                        bloqueadoAte =
                                agora.plusSeconds(
                                        janelaSegundos
                                );

                        bloqueioAtivado.set(
                                true
                        );
                    }

                    return new EstadoTentativas(
                            novasFalhas,
                            inicioDaJanela,
                            bloqueadoAte
                    );
                }
        );

        return bloqueioAtivado.get();
    }

    private EstadoTentativas
    validarEstadoParaAtualizacao(
            EstadoTentativas estado,
            Instant agora
    ) {
        if (estado == null) {
            return null;
        }

        if (
                estado.bloqueadoAte() != null
                        && !agora.isBefore(
                        estado.bloqueadoAte()
                )
        ) {
            return null;
        }

        if (
                estado.bloqueadoAte() == null
                        && !agora.isBefore(
                        estado.inicioDaJanela()
                                .plusSeconds(
                                        janelaSegundos
                                )
                )
        ) {
            return null;
        }

        return estado;
    }

    private void responderBloqueio(
            HttpServletRequest request,
            HttpServletResponse response,
            Instant bloqueadoAte,
            Instant agora
    ) throws IOException {
        long segundosRestantes =
                Math.max(
                        1,
                        Duration.between(
                                agora,
                                bloqueadoAte
                        ).toSeconds()
                );

        LOGGER.warn(
                "Login bloqueado temporariamente; ip={}; endpoint={}; retryAfter={}",
                request.getRemoteAddr(),
                request.getRequestURI(),
                segundosRestantes
        );

        response.setStatus(
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.setHeader(
                HttpHeaders.RETRY_AFTER,
                Long.toString(
                        segundosRestantes
                )
        );

        response.setHeader(
                HttpHeaders.CACHE_CONTROL,
                "no-store"
        );

        String corpo =
                """
                {
                  "status": 429,
                  "error": "Too Many Requests",
                  "message": "Muitas tentativas de login. Aguarde antes de tentar novamente.",
                  "path": "%s"
                }
                """.formatted(
                        escaparJson(
                                request.getRequestURI()
                        )
                );

        response.getWriter().write(
                corpo
        );
    }

    private void registrarBloqueioNaAuditoria(
            HttpServletRequest request
    ) {
        try {
            auditoriaService.registrar(
                    TipoAtorAuditoria.SISTEMA,
                    null,
                    AcaoAuditoria.ACESSO_NEGADO,
                    TipoRecursoAuditoria.API,
                    request.getRequestURI(),
                    null,
                    null,
                    ResultadoAuditoria.NEGADO,
                    ContextoAuditoria.from(
                            request
                    ),
                    "Login temporariamente bloqueado após "
                            + maximoFalhas
                            + " falhas em "
                            + janelaSegundos
                            + " segundos"
            );

        } catch (RuntimeException erroAuditoria) {
            LOGGER.error(
                    "Não foi possível registrar o bloqueio de login",
                    erroAuditoria
            );
        }
    }

    private void executarLimpezaPeriodica(
            Instant agora
    ) {
        long requisicoes =
                totalRequisicoes.incrementAndGet();

        if (requisicoes % 100 != 0) {
            return;
        }

        tentativasPorChave
                .entrySet()
                .removeIf(entrada -> {
                    EstadoTentativas estado =
                            entrada.getValue();

                    if (
                            estado.bloqueadoAte() != null
                    ) {
                        return !agora.isBefore(
                                estado.bloqueadoAte()
                        );
                    }

                    return !agora.isBefore(
                            estado.inicioDaJanela()
                                    .plusSeconds(
                                            janelaSegundos
                                    )
                    );
                });
    }

    private String escaparJson(
            String valor
    ) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                );
    }

    private record EstadoTentativas(
            int falhas,
            Instant inicioDaJanela,
            Instant bloqueadoAte
    ) {
    }
}