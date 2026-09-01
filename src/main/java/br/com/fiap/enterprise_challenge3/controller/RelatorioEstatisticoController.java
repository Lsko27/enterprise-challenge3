package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.relatorio.RelatorioEstatisticoResponse;
import br.com.fiap.enterprise_challenge3.service.RelatorioEstatisticoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/servidor/relatorios")
public class RelatorioEstatisticoController {

    private final RelatorioEstatisticoService relatorioService;

    public RelatorioEstatisticoController(
            RelatorioEstatisticoService relatorioService
    ) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<RelatorioEstatisticoResponse>
    gerarRelatorio(
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate inicio,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fim
    ) {
        return ResponseEntity.ok(
                relatorioService.gerarRelatorio(
                        inicio,
                        fim
                )
        );
    }
}