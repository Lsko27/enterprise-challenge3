package br.com.fiap.enterprise_challenge3.controller;

import br.com.fiap.enterprise_challenge3.dto.SubservicoRequest;
import br.com.fiap.enterprise_challenge3.dto.SubservicoResponse;
import br.com.fiap.enterprise_challenge3.service.SubservicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SubservicoController {

    private final SubservicoService subservicoService;

    public SubservicoController(
            SubservicoService subservicoService
    ) {
        this.subservicoService = subservicoService;
    }

    @GetMapping("/categorias/{categoriaId}/subservicos")
    public ResponseEntity<List<SubservicoResponse>>
    listarPorCategoria(
            @PathVariable Long categoriaId
    ) {
        return ResponseEntity.ok(
                subservicoService.listarPorCategoria(
                        categoriaId
                )
        );
    }

    @GetMapping("/subservicos/{id}")
    public ResponseEntity<SubservicoResponse>
    buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                subservicoService.buscarPorId(id)
        );
    }

    @PostMapping("/subservicos")
    public ResponseEntity<SubservicoResponse>
    cadastrar(
            @Valid @RequestBody SubservicoRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        subservicoService.cadastrar(request)
                );
    }

    @PutMapping("/subservicos/{id}")
    public ResponseEntity<SubservicoResponse>
    atualizar(
            @PathVariable Long id,
            @Valid @RequestBody SubservicoRequest request
    ) {
        return ResponseEntity.ok(
                subservicoService.atualizar(id, request)
        );
    }

    @PatchMapping("/subservicos/{id}/desativar")
    public ResponseEntity<Void> desativar(
            @PathVariable Long id
    ) {
        subservicoService.desativar(id);

        return ResponseEntity.noContent().build();
    }
}