package br.com.fiap.enterprise_challenge3.dto;

public record ServidorLoginResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        Long servidorId,
        String nome,
        String cargo,
        String mensagem
) {
}