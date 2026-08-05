package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.enums.NivelUrgencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record SolicitacaoCreateRequest(

        @NotBlank(message = "O título é obrigatório")
        @Size(
                max = 150,
                message = "O título deve possuir no máximo 150 caracteres"
        )
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(
                max = 2000,
                message = "A descrição deve possuir no máximo 2000 caracteres"
        )
        String descricao,

        @NotNull(message = "A urgência é obrigatória")
        NivelUrgencia urgencia,

        @NotNull(message = "O subserviço é obrigatório")
        Long subservicoId,

        @NotNull(message = "O endereço é obrigatório")
        @Valid
        EnderecoRequest endereco

) {
}