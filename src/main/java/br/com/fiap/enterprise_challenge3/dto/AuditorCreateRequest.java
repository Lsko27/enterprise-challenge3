package br.com.fiap.enterprise_challenge3.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuditorCreateRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(
                max = 150,
                message = "O nome deve possuir no máximo 150 caracteres"
        )
        String nome,

        @NotBlank(message = "A matrícula é obrigatória")
        @Size(
                max = 30,
                message = "A matrícula deve possuir no máximo 30 caracteres"
        )
        String matricula,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        @Size(
                max = 150,
                message = "O e-mail deve possuir no máximo 150 caracteres"
        )
        String email,

        @NotBlank(message = "O cargo é obrigatório")
        @Size(
                max = 100,
                message = "O cargo deve possuir no máximo 100 caracteres"
        )
        String cargo,

        @NotBlank(message = "A senha é obrigatória")
        @Size(
                min = 8,
                max = 72,
                message = "A senha deve possuir entre 8 e 72 caracteres"
        )
        String senha

) {
}