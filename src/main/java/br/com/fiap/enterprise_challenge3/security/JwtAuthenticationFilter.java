package br.com.fiap.enterprise_challenge3.security;

import br.com.fiap.enterprise_challenge3.model.Cidadao;
import br.com.fiap.enterprise_challenge3.model.Servidor;
import br.com.fiap.enterprise_challenge3.repository.CidadaoRepository;
import br.com.fiap.enterprise_challenge3.repository.ServidorRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String PERFIL_CIDADAO =
            "CIDADAO";

    private static final String PERFIL_SERVIDOR =
            "SERVIDOR";

    private static final String PERFIL_AUDITOR =
            "AUDITOR";

    private final JwtService jwtService;
    private final CidadaoRepository cidadaoRepository;
    private final ServidorRepository servidorRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CidadaoRepository cidadaoRepository,
            ServidorRepository servidorRepository
    ) {
        this.jwtService = jwtService;
        this.cidadaoRepository = cidadaoRepository;
        this.servidorRepository = servidorRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String cabecalho =
                request.getHeader(
                        HttpHeaders.AUTHORIZATION
                );

        if (!possuiBearerToken(cabecalho)) {
            continuarFiltro(
                    request,
                    response,
                    filterChain
            );

            return;
        }

        String token =
                cabecalho
                        .substring(
                                BEARER_PREFIX.length()
                        )
                        .trim();

        if (token.isBlank()) {
            SecurityContextHolder.clearContext();

            continuarFiltro(
                    request,
                    response,
                    filterChain
            );

            return;
        }

        try {
            autenticarToken(
                    token,
                    request
            );

        } catch (
                JwtException
                | IllegalArgumentException exception
        ) {
            /*
             * Um token inválido, expirado ou malformado
             * não deve autenticar a requisição.
             *
             * A rota protegida será posteriormente
             * respondida pelo AuthenticationEntryPoint.
             */
            SecurityContextHolder.clearContext();
        }

        continuarFiltro(
                request,
                response,
                filterChain
        );
    }

    private boolean possuiBearerToken(
            String cabecalho
    ) {
        return cabecalho != null
                && cabecalho.startsWith(
                BEARER_PREFIX
        );
    }

    private void autenticarToken(
            String token,
            HttpServletRequest request
    ) {
        if (
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        != null
        ) {
            return;
        }

        Long usuarioId =
                jwtService.extrairUsuarioId(
                        token
                );

        String perfilDoToken =
                normalizarPerfil(
                        jwtService.extrairPerfil(
                                token
                        )
                );

        switch (perfilDoToken) {
            case PERFIL_CIDADAO ->
                    autenticarCidadao(
                            usuarioId,
                            request
                    );

            case PERFIL_SERVIDOR,
                 PERFIL_AUDITOR ->
                    autenticarServidorOuAuditor(
                            usuarioId,
                            perfilDoToken,
                            request
                    );

            default -> {
                /*
                 * Perfis desconhecidos não recebem
                 * autenticação.
                 */
            }
        }
    }

    private String normalizarPerfil(
            String perfil
    ) {
        if (perfil == null || perfil.isBlank()) {
            throw new IllegalArgumentException(
                    "Perfil ausente no token"
            );
        }

        return perfil
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private void autenticarCidadao(
            Long cidadaoId,
            HttpServletRequest request
    ) {
        Cidadao cidadao =
                cidadaoRepository
                        .findById(
                                cidadaoId
                        )
                        .filter(usuario ->
                                Boolean.TRUE.equals(
                                        usuario.getAtivo()
                                )
                        )
                        .orElse(
                                null
                        );

        if (cidadao == null) {
            return;
        }

        criarAutenticacao(
                cidadao.getId(),
                PERFIL_CIDADAO,
                request
        );
    }

    private void autenticarServidorOuAuditor(
            Long servidorId,
            String perfilDoToken,
            HttpServletRequest request
    ) {
        Servidor servidor =
                servidorRepository
                        .findById(
                                servidorId
                        )
                        .filter(usuario ->
                                Boolean.TRUE.equals(
                                        usuario.getAtivo()
                                )
                        )
                        .orElse(
                                null
                        );

        if (
                servidor == null
                        || servidor.getPerfil() == null
        ) {
            return;
        }

        String perfilAtual =
                servidor
                        .getPerfil()
                        .name();

        /*
         * Impede que um token emitido para um perfil
         * diferente daquele atualmente registrado no
         * banco seja utilizado.
         *
         * Isso também invalida, na prática, tokens
         * antigos após uma alteração de perfil.
         */
        if (
                !perfilAtual.equals(
                        perfilDoToken
                )
        ) {
            return;
        }

        criarAutenticacao(
                servidor.getId(),
                perfilAtual,
                request
        );
    }

    private void criarAutenticacao(
            Long usuarioId,
            String perfil,
            HttpServletRequest request
    ) {
        String autoridade =
                "ROLE_" + perfil;

        List<SimpleGrantedAuthority> permissoes =
                List.of(
                        new SimpleGrantedAuthority(
                                autoridade
                        )
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        usuarioId.toString(),
                        null,
                        permissoes
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(
                                request
                        )
        );

        SecurityContext context =
                SecurityContextHolder
                        .createEmptyContext();

        context.setAuthentication(
                authentication
        );

        SecurityContextHolder.setContext(
                context
        );
    }

    private void continuarFiltro(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        filterChain.doFilter(
                request,
                response
        );
    }
}