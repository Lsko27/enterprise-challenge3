package br.com.fiap.enterprise_challenge3.dto.analiseia.python;

import java.util.List;

public record AnaliseIaPythonRequest(
        Long solicitacaoId,
        String titulo,
        String descricao,
        List<CategoriaIaItem> categorias,
        List<SubservicoIaItem> subservicos
) {
}