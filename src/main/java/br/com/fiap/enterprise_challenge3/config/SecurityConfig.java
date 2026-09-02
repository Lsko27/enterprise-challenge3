package br.com.fiap.enterprise_challenge3.config;

import br.com.fiap.enterprise_challenge3.security.AuditoriaAccessDeniedHandler;
import br.com.fiap.enterprise_challenge3.security.AuditoriaAuthenticationEntryPoint;
import br.com.fiap.enterprise_challenge3.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final AuditoriaAuthenticationEntryPoint
            authenticationEntryPoint;

    private final AuditoriaAccessDeniedHandler
            accessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuditoriaAuthenticationEntryPoint authenticationEntryPoint,
            AuditoriaAccessDeniedHandler accessDeniedHandler
    ) {
        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.authenticationEntryPoint =
                authenticationEntryPoint;

        this.accessDeniedHandler =
                accessDeniedHandler;
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

        configuracao.setExposedHeaders(
                List.of(
                        HttpHeaders.CONTENT_DISPOSITION
                )
        );

        /*
         * O navegador envia o cookie HttpOnly ao BFF
         * do Next.js.
         *
         * O BFF extrai o JWT do cookie e o envia ao
         * backend Java pelo header Authorization.
         *
         * O backend Java não utiliza autenticação
         * baseada em cookie.
         */
        configuracao.setAllowCredentials(
                false
        );

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
                 * O JWT chega pelo header Authorization.
                 * O backend não autentica por cookie.
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

                /*
                 * Registra na trilha as respostas 401 e 403.
                 */
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
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
                                 * Bootstrap local do primeiro auditor.
                                 *
                                 * O controller dessa rota só existe
                                 * quando o perfil "local" está ativo.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/setup/auditor"
                                ).permitAll()

                                /*
                                 * Cadastro público de cidadão.
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
                                 * Impede listagem geral de cidadãos
                                 * e consultas por ID.
                                 */
                                .requestMatchers(
                                        "/api/cidadaos/**"
                                ).denyAll()

                                /*
                                 * Trilhas completas de auditoria.
                                 * Apenas governança pode consultar.
                                 */
                                .requestMatchers(
                                        "/api/servidor/auditoria",
                                        "/api/servidor/auditoria/**"
                                ).hasRole(
                                        "AUDITOR"
                                )

                                /*
                                 * Relatórios estatísticos.
                                 * Servidores e auditores podem acessar.
                                 */
                                .requestMatchers(
                                        "/api/servidor/relatorios",
                                        "/api/servidor/relatorios/**"
                                ).hasAnyRole(
                                        "SERVIDOR",
                                        "AUDITOR"
                                )

                                /*
                                 * Consulta do próprio perfil de
                                 * servidor ou auditor.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/servidor/me"
                                ).hasAnyRole(
                                        "SERVIDOR",
                                        "AUDITOR"
                                )

                                /*
                                 * Funcionalidades operacionais.
                                 * Auditores não podem alterar ou
                                 * atender solicitações.
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
                                 * Alterações no catálogo não estão
                                 * expostas pela API pública.
                                 */
                                .requestMatchers(
                                        "/api/categorias/**",
                                        "/api/subservicos/**"
                                ).denyAll()

                                /*
                                 * Notificações exclusivas do cidadão.
                                 */
                                .requestMatchers(
                                        "/api/notificacoes/**"
                                ).hasRole(
                                        "CIDADAO"
                                )

                                /*
                                 * Solicitações exclusivas do cidadão.
                                 */
                                .requestMatchers(
                                        "/api/solicitacoes/**"
                                ).hasRole(
                                        "CIDADAO"
                                )

                                /*
                                 * Permite o tratamento interno
                                 * de respostas de erro.
                                 */
                                .requestMatchers(
                                        "/error"
                                ).permitAll()

                                /*
                                 * Bloqueia qualquer rota não declarada.
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