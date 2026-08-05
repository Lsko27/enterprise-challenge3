package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.Servidor;

import java.time.LocalDateTime;

public record ServidorResponse(
        Long id,
        String nome,
        String matricula,
        String email,
        String cargo,
        LocalDateTime dataCadastro,
        Boolean ativo
) {

    public static ServidorResponse fromEntity(
            Servidor servidor
    ) {
        return new ServidorResponse(
                servidor.getId(),
                servidor.getNome(),
                servidor.getMatricula(),
                servidor.getEmail(),
                servidor.getCargo(),
                servidor.getDataCadastro(),
                servidor.getAtivo()
        );
    }
}