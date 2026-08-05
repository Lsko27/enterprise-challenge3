package br.com.fiap.enterprise_challenge3.storage;

import org.springframework.core.io.Resource;

public record ArquivoDownload(
        Resource recurso,
        String nomeOriginal,
        String tipoConteudo,
        long tamanho
) {
}