package br.com.fiap.enterprise_challenge3.storage;

public record ArquivoArmazenado(
        String nomeOriginal,
        String nomeArmazenado,
        String tipoConteudo,
        long tamanho
) {
}