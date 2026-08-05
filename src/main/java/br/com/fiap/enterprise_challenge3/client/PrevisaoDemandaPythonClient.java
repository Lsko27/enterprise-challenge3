package br.com.fiap.enterprise_challenge3.client;

import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.PrevisaoDemandaIaRequest;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.PrevisaoDemandaIaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class PrevisaoDemandaPythonClient {

    private final JsonMapper jsonMapper;
    private final HttpClient httpClient;
    private final String iaUrl;

    public PrevisaoDemandaPythonClient(
            JsonMapper jsonMapper,
            @Value(
                    "${govatende.ia.url:"
                            + "http://127.0.0.1:8000}"
            )
            String iaUrl
    ) {
        this.jsonMapper = jsonMapper;
        this.iaUrl = iaUrl;

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public PrevisaoDemandaIaResponse prever(
            PrevisaoDemandaIaRequest request
    ) {
        try {
            byte[] corpoJson =
                    jsonMapper.writeValueAsBytes(request);

            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            iaUrl
                                                    + "/prever-demandas"
                                    )
                            )
                            .timeout(Duration.ofSeconds(60))
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "Accept",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest
                                            .BodyPublishers
                                            .ofByteArray(corpoJson)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers
                                    .ofString(
                                            StandardCharsets.UTF_8
                                    )
                    );

            if (
                    response.statusCode() < 200
                            || response.statusCode() >= 300
            ) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Erro retornado pela IA de previsão. "
                                + "Status: "
                                + response.statusCode()
                                + ". Resposta: "
                                + response.body()
                );
            }

            return jsonMapper.readValue(
                    response.body(),
                    PrevisaoDemandaIaResponse.class
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "A comunicação com a IA foi interrompida.",
                    exception
            );

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível comunicar com "
                            + "a IA de previsão.",
                    exception
            );
        }
    }
}