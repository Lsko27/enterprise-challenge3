package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.Subservico;

public record SubservicoResponse(
        Long id,
        String nome,
        String descricao,
        Boolean ativo,
        Long categoriaId,
        String categoriaNome
) {

    public static SubservicoResponse fromEntity(
            Subservico subservico
    ) {
        return new SubservicoResponse(
                subservico.getId(),
                subservico.getNome(),
                subservico.getDescricao(),
                subservico.getAtivo(),
                subservico.getCategoria().getId(),
                subservico.getCategoria().getNome()
        );
    }
}