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

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

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

        if (cabecalho == null ||
                !cabecalho.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                cabecalho.substring(7).trim();

        try {
            Long usuarioId =
                    jwtService.extrairUsuarioId(token);

            String perfil =
                    jwtService.extrairPerfil(token);

            boolean naoAutenticado =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null;

            if (naoAutenticado) {
                autenticarUsuario(
                        usuarioId,
                        perfil,
                        request
                );
            }

        } catch (JwtException |
                 IllegalArgumentException exception) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void autenticarUsuario(
            Long usuarioId,
            String perfil,
            HttpServletRequest request
    ) {
        if ("CIDADAO".equals(perfil)) {
            autenticarCidadao(usuarioId, request);
            return;
        }

        if ("SERVIDOR".equals(perfil)) {
            autenticarServidor(usuarioId, request);
        }
    }

    private void autenticarCidadao(
            Long cidadaoId,
            HttpServletRequest request
    ) {
        Cidadao cidadao = cidadaoRepository
                .findById(cidadaoId)
                .filter(usuario ->
                        Boolean.TRUE.equals(
                                usuario.getAtivo()
                        )
                )
                .orElse(null);

        if (cidadao == null) {
            return;
        }

        criarAutenticacao(
                cidadao.getId(),
                "ROLE_CIDADAO",
                request
        );
    }

    private void autenticarServidor(
            Long servidorId,
            HttpServletRequest request
    ) {
        Servidor servidor = servidorRepository
                .findById(servidorId)
                .filter(usuario ->
                        Boolean.TRUE.equals(
                                usuario.getAtivo()
                        )
                )
                .orElse(null);

        if (servidor == null) {
            return;
        }

        criarAutenticacao(
                servidor.getId(),
                "ROLE_SERVIDOR",
                request
        );
    }

    private void criarAutenticacao(
            Long usuarioId,
            String autoridade,
            HttpServletRequest request
    ) {
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
                        .buildDetails(request)
        );

        SecurityContext context =
                SecurityContextHolder
                        .createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }
}