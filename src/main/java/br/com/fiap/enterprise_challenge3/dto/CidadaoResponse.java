package br.com.fiap.enterprise_challenge3.dto;

import br.com.fiap.enterprise_challenge3.model.Cidadao;

import java.time.LocalDateTime;

public record CidadaoResponse(
        Long id,
        String nome,
        String cpfMascarado,
        String email,
        String telefone,
        LocalDateTime dataCadastro,
        Boolean ativo
) {

    public static CidadaoResponse fromEntity(Cidadao cidadao) {
        return new CidadaoResponse(
                cidadao.getId(),
                cidadao.getNome(),
                mascararCpf(cidadao.getCpf()),
                cidadao.getEmail(),
                cidadao.getTelefone(),
                cidadao.getDataCadastro(),
                cidadao.getAtivo()
        );
    }

    private static String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return null;
        }

        return "***."
                + cpf.substring(3, 6)
                + "."
                + cpf.substring(6, 9)
                + "-**";
    }
}