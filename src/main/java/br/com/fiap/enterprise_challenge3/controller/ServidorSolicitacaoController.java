package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.AtualizarStatusSolicitacaoRequest;
import br.com.fiap.enterprise_challenge3.dto.HistoricoSolicitacaoResponse;
import br.com.fiap.enterprise_challenge3.dto.ItemFilaTriagemResponse;
import br.com.fiap.enterprise_challenge3.dto.ServidorSolicitacaoResponse;
import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import br.com.fiap.enterprise_challenge3.service.RegistroAuditoriaService;
import br.com.fiap.enterprise_challenge3.service.ServidorSolicitacaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/servidor/solicitacoes")
public class ServidorSolicitacaoController {

    private final ServidorSolicitacaoService solicitacaoService;
    private final RegistroAuditoriaService auditoriaService;

    public ServidorSolicitacaoController(
            ServidorSolicitacaoService solicitacaoService,
            RegistroAuditoriaService auditoriaService
    ) {
        this.solicitacaoService = solicitacaoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<List<ServidorSolicitacaoResponse>>
    listarTodas(
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Long servidorId =
                extrairServidorId(authentication);

        List<ServidorSolicitacaoResponse> resposta =
                solicitacaoService.listarTodas();

        registrarConsulta(
                servidorId,
                "LISTAGEM_GERAL",
                "Servidor consultou a listagem geral de solicitações",
                httpRequest
        );

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/fila-triagem")
    public ResponseEntity<List<ItemFilaTriagemResponse>>
    listarFilaTriagem(
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Long servidorId =
                extrairServidorId(authentication);

        List<ItemFilaTriagemResponse> resposta =
                solicitacaoService.listarFilaTriagem();

        registrarConsulta(
                servidorId,
                "FILA_TRIAGEM",
                "Servidor consultou a fila de triagem",
                httpRequest
        );

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServidorSolicitacaoResponse>
    buscarPorId(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Long servidorId =
                extrairServidorId(authentication);

        ServidorSolicitacaoResponse resposta =
                solicitacaoService.buscarPorId(id);

        registrarConsulta(
                servidorId,
                id.toString(),
                "Servidor consultou os detalhes de uma solicitação",
                httpRequest
        );

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoSolicitacaoResponse>>
    listarHistorico(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Long servidorId =
                extrairServidorId(authentication);

        List<HistoricoSolicitacaoResponse> resposta =
                solicitacaoService.listarHistorico(id);

        registrarConsulta(
                servidorId,
                id.toString(),
                "Servidor consultou o histórico de uma solicitação",
                httpRequest
        );

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}/historico/reverso")
    public ResponseEntity<List<HistoricoSolicitacaoResponse>>
    listarHistoricoReverso(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        Long servidorId =
                extrairServidorId(authentication);

        List<HistoricoSolicitacaoResponse> resposta =
                solicitacaoService.listarHistoricoReverso(id);

        registrarConsulta(
                servidorId,
                id.toString(),
                "Servidor consultou o histórico reverso de uma solicitação",
                httpRequest
        );

        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServidorSolicitacaoResponse>
    atualizarStatus(
            @PathVariable Long id,

            @Valid @RequestBody
            AtualizarStatusSolicitacaoRequest request,

            Authentication authentication,

            HttpServletRequest httpRequest
    ) {
        Long servidorId =
                extrairServidorId(authentication);

        return ResponseEntity.ok(
                solicitacaoService.atualizarStatus(
                        id,
                        request,
                        servidorId,
                        ContextoAuditoria.from(httpRequest)
                )
        );
    }

    private void registrarConsulta(
            Long servidorId,
            String recursoId,
            String detalhe,
            HttpServletRequest httpRequest
    ) {
        auditoriaService.registrar(
                TipoAtorAuditoria.SERVIDOR,
                servidorId,
                AcaoAuditoria.CONSULTA_DADOS_PESSOAIS,
                TipoRecursoAuditoria.SOLICITACAO,
                recursoId,
                null,
                null,
                ResultadoAuditoria.SUCESSO,
                ContextoAuditoria.from(httpRequest),
                detalhe
        );
    }

    private Long extrairServidorId(
            Authentication authentication
    ) {
        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Servidor não autenticado"
            );
        }

        try {
            return Long.valueOf(
                    authentication.getName()
            );

        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Identificação do servidor inválida"
            );
        }
    }
}