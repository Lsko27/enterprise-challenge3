package br.com.fiap.enterprise_challenge3.dto;

import jakarta.validation.constraints.NotBlank;

public record ServidorLoginRequest(

        @NotBlank(message = "A matrícula é obrigatória")
        String matricula,

        @NotBlank(message = "A senha é obrigatória")
        String senha

) {
}