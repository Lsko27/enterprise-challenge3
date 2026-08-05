package br.com.fiap.enterprise_challenge3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(

        @NotBlank(message = "O nome da categoria é obrigatório")
        @Size(max = 100, message = "O nome deve possuir no máximo 100 caracteres")
        String nome,

        @Size(max = 255, message = "A descrição deve possuir no máximo 255 caracteres")
        String descricao

) {
}