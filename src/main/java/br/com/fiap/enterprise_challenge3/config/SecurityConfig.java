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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origin:http://localhost:3000}")
            String origemPermitida
    ) {
        CorsConfiguration configuracao =
                new CorsConfiguration();

        configuracao.setAllowedOrigins(
                List.of(origemPermitida)
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
         * O projeto usa JWT no header Authorization,
         * e não autenticação baseada em cookies.
         */
        configuracao.setAllowCredentials(false);

        /*
         * Mantém o resultado do preflight em cache
         * durante uma hora.
         */
        configuracao.setMaxAge(3600L);

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

                .csrf(csrf -> csrf.disable())

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

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
                                 * Login público do servidor.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/auth/servidor/login"
                                ).permitAll()

                                /*
                                 * Cadastro público do cidadão.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/cidadaos"
                                ).permitAll()

                                /*
                                 * Rotas do próprio cidadão.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/cidadaos/me"
                                ).hasRole("CIDADAO")

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/cidadaos/me"
                                ).hasRole("CIDADAO")

                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/cidadaos/me/desativar"
                                ).hasRole("CIDADAO")

                                /*
                                 * Bloqueia listagem geral e acesso
                                 * a cidadãos por ID.
                                 */
                                .requestMatchers(
                                        "/api/cidadaos/**"
                                ).denyAll()

                                /*
                                 * Rotas exclusivas do servidor.
                                 */
                                .requestMatchers(
                                        "/api/servidor/**"
                                ).hasRole("SERVIDOR")

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
                                 * Notificações do cidadão.
                                 */
                                .requestMatchers(
                                        "/api/notificacoes/**"
                                ).hasRole("CIDADAO")

                                /*
                                 * Solicitações do cidadão.
                                 */
                                .requestMatchers(
                                        "/api/solicitacoes/**"
                                ).hasRole("CIDADAO")

                                /*
                                 * Tratamento interno de erros.
                                 */
                                .requestMatchers(
                                        "/error"
                                ).permitAll()

                                /*
                                 * Bloqueia rotas não declaradas.
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