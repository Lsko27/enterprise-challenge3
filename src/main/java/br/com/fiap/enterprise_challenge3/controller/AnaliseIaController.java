package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.analiseia.AnaliseIaResponse;
import br.com.fiap.enterprise_challenge3.service.AnaliseIaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/servidor/solicitacoes/{solicitacaoId}/analise-ia"
)
public class AnaliseIaController {

    private final AnaliseIaService analiseIaService;

    public AnaliseIaController(
            AnaliseIaService analiseIaService
    ) {
        this.analiseIaService = analiseIaService;
    }

    @PostMapping
    public ResponseEntity<AnaliseIaResponse>
    gerarAnalise(
            @PathVariable Long solicitacaoId
    ) {
        AnaliseIaResponse response =
                analiseIaService
                        .gerarAnalise(
                                solicitacaoId
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<AnaliseIaResponse>
    buscarAnaliseMaisRecente(
            @PathVariable Long solicitacaoId
    ) {
        return ResponseEntity.ok(
                analiseIaService
                        .buscarAnaliseMaisRecente(
                                solicitacaoId
                        )
        );
    }

    @GetMapping("/historico")
    public ResponseEntity<List<AnaliseIaResponse>>
    listarHistorico(
            @PathVariable Long solicitacaoId
    ) {
        return ResponseEntity.ok(
                analiseIaService
                        .listarHistorico(
                                solicitacaoId
                        )
        );
    }
}