package br.com.fiap.enterprise_challenge3.config;

import br.com.fiap.enterprise_challenge3.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource(
            @Value(
                    "${app.cors.allowed-origin:http://localhost:3000}"
            )
            String origemPermitida
    ) {
        CorsConfiguration configuracao =
                new CorsConfiguration();

        configuracao.setAllowedOrigins(
                List.of(
                        origemPermitida
                )
        );

        configuracao.setAllowedMethods(
                List.of(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()
                )
        );

        configuracao.setAllowedHeaders(
                List.of(
                        HttpHeaders.AUTHORIZATION,
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCEPT
                )
        );

        /*
         * Permite que o frontend leia o nome original
         * dos anexos durante o download.
         */
        configuracao.setExposedHeaders(
                List.of(
                        HttpHeaders.CONTENT_DISPOSITION
                )
        );

        /*
         * O navegador se comunica com o BFF do Next.js
         * por meio de cookie HttpOnly.
         *
         * O BFF envia o JWT ao backend Java pelo header
         * Authorization. Por isso, o backend não precisa
         * aceitar credenciais CORS baseadas em cookies.
         */
        configuracao.setAllowCredentials(
                false
        );

        /*
         * Mantém o resultado do preflight em cache
         * durante uma hora.
         */
        configuracao.setMaxAge(
                3600L
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuracao
        );

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource
                        )
                )

                /*
                 * O backend Java não autentica por cookie.
                 * O JWT chega pelo header Authorization
                 * enviado pelo BFF.
                 */
                .csrf(csrf ->
                        csrf.disable()
                )

                .formLogin(form ->
                        form.disable()
                )

                .httpBasic(basic ->
                        basic.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        new HttpStatusEntryPoint(
                                                HttpStatus.UNAUTHORIZED
                                        )
                                )
                                .accessDeniedHandler(
                                        new HttpStatusAccessDeniedHandler(
                                                HttpStatus.FORBIDDEN
                                        )
                                )
                )

                .authorizeHttpRequests(authorize ->
                        authorize

                                /*
                                 * Login público do cidadão.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/auth/login"
                                ).permitAll()

                                /*
                                 * Login público de servidores
                                 * e auditores.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/auth/servidor/login"
                                ).permitAll()

                                /*
                                 * Bootstrap local do primeiro
                                 * auditor.
                                 *
                                 * O controller só existe no
                                 * perfil Spring "local" e exige
                                 * o header X-Setup-Key.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/setup/auditor"
                                ).permitAll()

                                /*
                                 * Cadastro público do cidadão.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/cidadaos"
                                ).permitAll()

                                /*
                                 * Consulta do próprio perfil
                                 * do cidadão.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/cidadaos/me"
                                ).hasRole(
                                        "CIDADAO"
                                )

                                /*
                                 * Atualização do próprio perfil
                                 * do cidadão.
                                 */
                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/cidadaos/me"
                                ).hasRole(
                                        "CIDADAO"
                                )

                                /*
                                 * Desativação da própria conta
                                 * do cidadão.
                                 */
                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/cidadaos/me/desativar"
                                ).hasRole(
                                        "CIDADAO"
                                )

                                /*
                                 * Bloqueia listagem geral e
                                 * acesso a cidadãos por ID.
                                 */
                                .requestMatchers(
                                        "/api/cidadaos/**"
                                ).denyAll()

                                /*
                                 * Trilhas completas de auditoria.
                                 *
                                 * Somente o perfil de governança
                                 * pode acessar.
                                 */
                                .requestMatchers(
                                        "/api/servidor/auditoria/**"
                                ).hasRole(
                                        "AUDITOR"
                                )

                                /*
                                 * Relatórios estatísticos.
                                 *
                                 * Servidores operacionais e
                                 * auditores podem consultar.
                                 */
                                .requestMatchers(
                                        "/api/servidor/relatorios/**"
                                ).hasAnyRole(
                                        "SERVIDOR",
                                        "AUDITOR"
                                )

                                /*
                                 * Consulta do próprio perfil
                                 * de servidor ou auditor.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/servidor/me"
                                ).hasAnyRole(
                                        "SERVIDOR",
                                        "AUDITOR"
                                )

                                /*
                                 * Demais funcionalidades do
                                 * servidor, incluindo atendimento
                                 * e alteração de solicitações.
                                 *
                                 * Auditores não possuem acesso.
                                 */
                                .requestMatchers(
                                        "/api/servidor/**"
                                ).hasRole(
                                        "SERVIDOR"
                                )

                                /*
                                 * Status público da API.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/status"
                                ).permitAll()

                                /*
                                 * Consultas públicas do catálogo.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/categorias/**"
                                ).permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/subservicos/**"
                                ).permitAll()

                                /*
                                 * Bloqueia alterações no catálogo.
                                 */
                                .requestMatchers(
                                        "/api/categorias/**",
                                        "/api/subservicos/**"
                                ).denyAll()

                                /*
                                 * Notificações exclusivas
                                 * do cidadão autenticado.
                                 */
                                .requestMatchers(
                                        "/api/notificacoes/**"
                                ).hasRole(
                                        "CIDADAO"
                                )

                                /*
                                 * Solicitações exclusivas
                                 * do cidadão autenticado.
                                 */
                                .requestMatchers(
                                        "/api/solicitacoes/**"
                                ).hasRole(
                                        "CIDADAO"
                                )

                                /*
                                 * Tratamento interno de erros.
                                 */
                                .requestMatchers(
                                        "/error"
                                ).permitAll()

                                /*
                                 * Qualquer rota que não tenha
                                 * sido declarada será bloqueada.
                                 */
                                .anyRequest()
                                .denyAll()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}