package br.com.fiap.enterprise_challenge3.dto;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        Long cidadaoId,
        String nome,
        String mensagem
) {
}