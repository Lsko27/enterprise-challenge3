package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.GerarPrevisaoDemandaRequest;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.PrevisaoDemandaConsultaResponse;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.PrevisaoDemandaIaResponse;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.ResumoPrevisaoDemandaResponse;
import br.com.fiap.enterprise_challenge3.dto.previsaodemanda.HistoricoPrevisaoDemandaResponse;
import br.com.fiap.enterprise_challenge3.service.PrevisaoDemandaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        "/api/servidor/previsoes-demanda"
)
public class PrevisaoDemandaController {

    private final PrevisaoDemandaService
            previsaoDemandaService;

    public PrevisaoDemandaController(
            PrevisaoDemandaService
                    previsaoDemandaService
    ) {
        this.previsaoDemandaService =
                previsaoDemandaService;
    }

    @PostMapping("/gerar")
    public ResponseEntity<PrevisaoDemandaIaResponse>
    gerarPrevisoes(
            @RequestBody(required = false)
            GerarPrevisaoDemandaRequest request
    ) {
        GerarPrevisaoDemandaRequest parametros =
                request == null
                        ? new GerarPrevisaoDemandaRequest(
                        null,
                        null,
                        null,
                        false
                )
                        : request;

        return ResponseEntity.ok(
                previsaoDemandaService
                        .gerarPrevisoes(
                                parametros
                        )
        );
    }

    @GetMapping("/ultima")
    public ResponseEntity<
            List<PrevisaoDemandaConsultaResponse>
            >
    consultarUltimaGeracao() {

        return ResponseEntity.ok(
                previsaoDemandaService
                        .consultarUltimaGeracao()
        );
    }

    @GetMapping("/historico")
    public ResponseEntity<
            List<HistoricoPrevisaoDemandaResponse>
            >
    consultarHistorico() {

        return ResponseEntity.ok(
                previsaoDemandaService
                        .consultarHistoricoGeracoes()
        );
    }

    @GetMapping("/resumo")
    public ResponseEntity<
            ResumoPrevisaoDemandaResponse
            >
    consultarResumo() {

        return ResponseEntity.ok(
                previsaoDemandaService
                        .consultarResumoUltimaGeracao()
        );
    }
}