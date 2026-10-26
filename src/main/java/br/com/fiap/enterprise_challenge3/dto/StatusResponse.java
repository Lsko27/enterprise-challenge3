package br.com.fiap.enterprise_challenge3.dto;

import java.time.Instant;

public record StatusResponse(
        String status,
        String aplicacao,
        String bancoDados,
        Instant dataHora
) {
}