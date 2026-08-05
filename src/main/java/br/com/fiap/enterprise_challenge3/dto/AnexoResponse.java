package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.Anexo;

import java.time.LocalDateTime;

public record AnexoResponse(
        Long id,
        String nomeOriginal,
        String tipoConteudo,
        Long tamanho,
        LocalDateTime dataEnvio
) {

    public static AnexoResponse fromEntity(
            Anexo anexo
    ) {
        return new AnexoResponse(
                anexo.getId(),
                anexo.getNomeOriginal(),
                anexo.getTipoConteudo(),
                anexo.getTamanho(),
                anexo.getDataEnvio()
        );
    }
}