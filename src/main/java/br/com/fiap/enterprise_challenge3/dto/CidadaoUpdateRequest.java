package br.com.fiap.enterprise_challenge3.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CidadaoUpdateRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve possuir no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail informado é inválido")
        @Size(max = 150, message = "O e-mail deve possuir no máximo 150 caracteres")
        String email,

        @Size(max = 20, message = "O telefone deve possuir no máximo 20 caracteres")
        String telefone

) {
}