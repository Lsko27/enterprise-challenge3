package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.enums.PerfilServidor;

public record ServidorLoginResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        Long servidorId,
        String nome,
        String cargo,
        PerfilServidor perfil,
        String mensagem
) {
}