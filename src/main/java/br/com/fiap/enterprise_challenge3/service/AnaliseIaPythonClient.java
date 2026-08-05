package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.analiseia.python.AnaliseIaPythonRequest;
import br.com.fiap.enterprise_challenge3.dto.analiseia.python.AnaliseIaPythonResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class AnaliseIaPythonClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;

    public AnaliseIaPythonClient(
            ObjectMapper objectMapper,
            @Value("${govatende.ia.base-url}")
            String baseUrl
    ) {
        this.objectMapper = objectMapper;
        this.baseUrl = removerBarraFinal(baseUrl);

        this.httpClient = HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public AnaliseIaPythonResponse analisar(
            AnaliseIaPythonRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "A requisição enviada ao serviço de IA está nula"
            );
        }

        String jsonRequest = converterRequestParaJson(request);

        System.out.println(
                "JSON enviado para a IA: " + jsonRequest
        );

        HttpRequest httpRequest = HttpRequest
                .newBuilder()
                .uri(
                        URI.create(
                                baseUrl + "/analisar"
                        )
                )
                .timeout(Duration.ofSeconds(15))
                .header(
                        "Content-Type",
                        "application/json"
                )
                .header(
                        "Accept",
                        "application/json"
                )
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                jsonRequest,
                                StandardCharsets.UTF_8
                        )
                )
                .build();

        HttpResponse<String> httpResponse =
                enviarRequisicao(httpRequest);

        validarStatusResposta(httpResponse);

        return converterResposta(
                httpResponse.body()
        );
    }

    private String converterRequestParaJson(
            AnaliseIaPythonRequest request
    ) {
        try {
            return objectMapper
                    .writeValueAsString(request);

        } catch (JacksonException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível converter os dados enviados à IA",
                    exception
            );
        }
    }

    private HttpResponse<String> enviarRequisicao(
            HttpRequest request
    ) {
        try {
            return httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8
                    )
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "A comunicação com o serviço de IA foi interrompida",
                    exception
            );

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "O serviço de IA está indisponível",
                    exception
            );
        }
    }

    private void validarStatusResposta(
            HttpResponse<String> response
    ) {
        int status = response.statusCode();

        if (status < 200 || status >= 300) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Erro retornado pelo serviço de IA: "
                            + response.body()
            );
        }
    }

    private AnaliseIaPythonResponse converterResposta(
            String jsonResponse
    ) {
        if (
                jsonResponse == null
                        || jsonResponse.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "O serviço de IA retornou uma resposta vazia"
            );
        }

        try {
            return objectMapper.readValue(
                    jsonResponse,
                    AnaliseIaPythonResponse.class
            );

        } catch (JacksonException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível interpretar a resposta da IA: "
                            + jsonResponse,
                    exception
            );
        }
    }

    private static String removerBarraFinal(
            String url
    ) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "A URL do serviço de IA não foi configurada"
            );
        }

        return url.endsWith("/")
                ? url.substring(0, url.length() - 1)
                : url;
    }
}