package br.com.fiap.enterprise_challenge3.dto.relatorio;

import java.time.LocalDate;

public record SerieTemporalDiariaResponse(
        LocalDate data,
        long quantidade
) {
}