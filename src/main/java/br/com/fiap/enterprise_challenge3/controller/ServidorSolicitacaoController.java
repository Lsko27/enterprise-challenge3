package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.AtualizarStatusSolicitacaoRequest;
import br.com.fiap.enterprise_challenge3.dto.HistoricoSolicitacaoResponse;
import br.com.fiap.enterprise_challenge3.dto.ServidorSolicitacaoResponse;
import br.com.fiap.enterprise_challenge3.service.ServidorSolicitacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servidor/solicitacoes")
public class ServidorSolicitacaoController {

    private final ServidorSolicitacaoService solicitacaoService;

    public ServidorSolicitacaoController(
            ServidorSolicitacaoService solicitacaoService
    ) {
        this.solicitacaoService = solicitacaoService;
    }

    @GetMapping
    public ResponseEntity<List<ServidorSolicitacaoResponse>>
    listarTodas() {

        return ResponseEntity.ok(
                solicitacaoService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServidorSolicitacaoResponse>
    buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                solicitacaoService.buscarPorId(id)
        );
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoSolicitacaoResponse>>
    listarHistorico(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                solicitacaoService.listarHistorico(id)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServidorSolicitacaoResponse>
    atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody
            AtualizarStatusSolicitacaoRequest request
    ) {
        return ResponseEntity.ok(
                solicitacaoService.atualizarStatus(
                        id,
                        request
                )
        );
    }
}