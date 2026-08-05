package br.com.fiap.enterprise_challenge3.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ArquivoStorageService {

    private static final long TAMANHO_MAXIMO =
            5L * 1024 * 1024;

    private static final Map<String, String>
            EXTENSOES_POR_TIPO = Map.of(
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "application/pdf", ".pdf"
    );

    private static final Map<String, String>
            TIPOS_POR_EXTENSAO = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "pdf", "application/pdf"
    );

    private final Path diretorioBase;

    public ArquivoStorageService(
            @Value("${app.upload.dir:uploads}")
            String diretorioConfigurado
    ) {
        this.diretorioBase = Paths
                .get(diretorioConfigurado)
                .toAbsolutePath()
                .normalize();
    }

    @PostConstruct
    public void inicializar() {
        try {
            Files.createDirectories(diretorioBase);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Não foi possível criar o diretório de uploads",
                    exception
            );
        }
    }

    public ArquivoArmazenado salvar(
            MultipartFile arquivo
    ) {
        validarArquivo(arquivo);

        String nomeOriginal =
                obterNomeOriginal(arquivo);

        String tipoConteudo =
                resolverTipoConteudo(
                        arquivo,
                        nomeOriginal
                );

        String extensao =
                EXTENSOES_POR_TIPO.get(
                        tipoConteudo
                );

        String nomeArmazenado =
                UUID.randomUUID() + extensao;

        Path destino = diretorioBase
                .resolve(nomeArmazenado)
                .normalize();

        validarCaminho(destino);

        try {
            Files.copy(
                    arquivo.getInputStream(),
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível armazenar o arquivo"
            );
        }

        return new ArquivoArmazenado(
                nomeOriginal,
                nomeArmazenado,
                tipoConteudo,
                arquivo.getSize()
        );
    }

    public Resource carregar(
            String nomeArmazenado
    ) {
        Path caminho = diretorioBase
                .resolve(nomeArmazenado)
                .normalize();

        validarCaminho(caminho);

        Resource recurso = new PathResource(caminho);

        if (!recurso.exists() || !recurso.isReadable()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Arquivo não encontrado"
            );
        }

        return recurso;
    }

    public void excluir(
            String nomeArmazenado
    ) {
        Path caminho = diretorioBase
                .resolve(nomeArmazenado)
                .normalize();

        validarCaminho(caminho);

        try {
            Files.deleteIfExists(caminho);

        } catch (IOException exception) {
            // Impede que uma falha de limpeza esconda
            // o erro original do banco de dados.
        }
    }

    private void validarArquivo(
            MultipartFile arquivo
    ) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O arquivo é obrigatório"
            );
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "O arquivo deve possuir no máximo 5 MB"
            );
        }
    }

    private String obterNomeOriginal(
            MultipartFile arquivo
    ) {
        String nome = Optional
                .ofNullable(
                        arquivo.getOriginalFilename()
                )
                .orElse("arquivo");

        nome = StringUtils.cleanPath(nome);

        if (nome.isBlank() || nome.contains("..")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nome de arquivo inválido"
            );
        }

        return nome;
    }

    private String resolverTipoConteudo(
            MultipartFile arquivo,
            String nomeOriginal
    ) {
        String tipoInformado =
                Optional.ofNullable(
                                arquivo.getContentType()
                        )
                        .orElse("")
                        .toLowerCase(Locale.ROOT);

        if (EXTENSOES_POR_TIPO.containsKey(
                tipoInformado
        )) {
            return tipoInformado.equals("image/jpg")
                    ? "image/jpeg"
                    : tipoInformado;
        }

        String extensao = StringUtils
                .getFilenameExtension(nomeOriginal);

        if (extensao != null) {
            String tipoPelaExtensao =
                    TIPOS_POR_EXTENSAO.get(
                            extensao.toLowerCase(Locale.ROOT)
                    );

            if (tipoPelaExtensao != null &&
                    (tipoInformado.isBlank()
                            || tipoInformado.equals(
                            "application/octet-stream"
                    ))) {

                return tipoPelaExtensao;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Formato não permitido. Use JPG, PNG, WEBP ou PDF"
        );
    }

    private void validarCaminho(
            Path caminho
    ) {
        if (!caminho.startsWith(diretorioBase)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Caminho de arquivo inválido"
            );
        }
    }
}