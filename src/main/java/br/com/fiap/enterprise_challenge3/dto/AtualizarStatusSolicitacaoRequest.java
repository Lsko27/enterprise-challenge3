package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.enums.StatusSolicitacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarStatusSolicitacaoRequest(

        @NotNull(message = "O novo status é obrigatório")
        StatusSolicitacao novoStatus,

        @NotBlank(message = "A observação é obrigatória")
        @Size(
                min = 5,
                max = 500,
                message = "A observação deve possuir entre 5 e 500 caracteres"
        )
        String observacao

) {
}