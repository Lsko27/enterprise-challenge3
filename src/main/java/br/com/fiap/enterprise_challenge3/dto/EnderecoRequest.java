package br.com.fiap.enterprise_challenge3.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record EnderecoRequest(

        @NotBlank(message = "O logradouro é obrigatório")
        @Size(max = 150)
        String logradouro,

        @Size(max = 20)
        String numero,

        @Size(max = 100)
        String complemento,

        @NotBlank(message = "O bairro é obrigatório")
        @Size(max = 100)
        String bairro,

        @NotBlank(message = "A cidade é obrigatória")
        @Size(max = 100)
        String cidade,

        @NotBlank(message = "O estado é obrigatório")
        @Pattern(
                regexp = "^[A-Za-z]{2}$",
                message = "O estado deve possuir duas letras"
        )
        String estado,

        @Pattern(
                regexp = "^\\d{5}-?\\d{3}$",
                message = "O CEP deve possuir oito dígitos"
        )
        String cep,

        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        BigDecimal longitude

) {
}