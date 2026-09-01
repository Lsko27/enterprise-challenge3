package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.AtualizarStatusSolicitacaoRequest;
import br.com.fiap.enterprise_challenge3.dto.HistoricoSolicitacaoResponse;
import br.com.fiap.enterprise_challenge3.dto.ItemFilaTriagemResponse;
import br.com.fiap.enterprise_challenge3.dto.ServidorSolicitacaoResponse;
import br.com.fiap.enterprise_challenge3.service.ServidorSolicitacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/fila-triagem")
    public ResponseEntity<List<ItemFilaTriagemResponse>>
    listarFilaTriagem() {
        return ResponseEntity.ok(
                solicitacaoService.listarFilaTriagem()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServidorSolicitacaoResponse>
    buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                solicitacaoService.buscarPorId(
                        id
                )
        );
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoSolicitacaoResponse>>
    listarHistorico(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                solicitacaoService.listarHistorico(
                        id
                )
        );
    }

    @GetMapping("/{id}/historico/reverso")
    public ResponseEntity<List<HistoricoSolicitacaoResponse>>
    listarHistoricoReverso(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                solicitacaoService.listarHistoricoReverso(
                        id
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServidorSolicitacaoResponse>
    atualizarStatus(
            @PathVariable Long id,
            @Valid
            @RequestBody
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