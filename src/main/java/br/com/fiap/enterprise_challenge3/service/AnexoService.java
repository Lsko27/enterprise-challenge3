package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.AnexoResponse;
import br.com.fiap.enterprise_challenge3.model.Anexo;
import br.com.fiap.enterprise_challenge3.model.Solicitacao;
import br.com.fiap.enterprise_challenge3.model.enums.StatusSolicitacao;
import br.com.fiap.enterprise_challenge3.repository.AnexoRepository;
import br.com.fiap.enterprise_challenge3.repository.SolicitacaoRepository;
import br.com.fiap.enterprise_challenge3.storage.ArquivoArmazenado;
import br.com.fiap.enterprise_challenge3.storage.ArquivoDownload;
import br.com.fiap.enterprise_challenge3.storage.ArquivoStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class AnexoService {

    private static final long LIMITE_ANEXOS = 5;

    private final AnexoRepository anexoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final ArquivoStorageService storageService;

    public AnexoService(
            AnexoRepository anexoRepository,
            SolicitacaoRepository solicitacaoRepository,
            ArquivoStorageService storageService
    ) {
        this.anexoRepository = anexoRepository;
        this.solicitacaoRepository =
                solicitacaoRepository;
        this.storageService = storageService;
    }

    public AnexoResponse adicionarDoCidadao(
            Long solicitacaoId,
            Long cidadaoId,
            MultipartFile arquivo
    ) {
        Solicitacao solicitacao =
                encontrarSolicitacaoDoCidadao(
                        solicitacaoId,
                        cidadaoId
                );

        validarStatusParaAnexo(solicitacao);

        long quantidade =
                anexoRepository
                        .countBySolicitacao_Id(
                                solicitacaoId
                        );

        if (quantidade >= LIMITE_ANEXOS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A solicitação já possui o limite de 5 anexos"
            );
        }

        ArquivoArmazenado armazenado =
                storageService.salvar(arquivo);

        try {
            Anexo anexo = new Anexo(
                    armazenado.nomeOriginal(),
                    armazenado.nomeArmazenado(),
                    armazenado.tipoConteudo(),
                    armazenado.tamanho(),
                    solicitacao
            );

            Anexo anexoSalvo =
                    anexoRepository.saveAndFlush(anexo);

            return AnexoResponse.fromEntity(
                    anexoSalvo
            );

        } catch (RuntimeException exception) {
            storageService.excluir(
                    armazenado.nomeArmazenado()
            );

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<AnexoResponse> listarDoCidadao(
            Long solicitacaoId,
            Long cidadaoId
    ) {
        encontrarSolicitacaoDoCidadao(
                solicitacaoId,
                cidadaoId
        );

        return listarAnexos(solicitacaoId);
    }

    @Transactional(readOnly = true)
    public ArquivoDownload baixarDoCidadao(
            Long solicitacaoId,
            Long anexoId,
            Long cidadaoId
    ) {
        encontrarSolicitacaoDoCidadao(
                solicitacaoId,
                cidadaoId
        );

        return prepararDownload(
                solicitacaoId,
                anexoId
        );
    }

    @Transactional(readOnly = true)
    public List<AnexoResponse> listarParaServidor(
            Long solicitacaoId
    ) {
        encontrarSolicitacaoParaServidor(
                solicitacaoId
        );

        return listarAnexos(solicitacaoId);
    }

    @Transactional(readOnly = true)
    public ArquivoDownload baixarParaServidor(
            Long solicitacaoId,
            Long anexoId
    ) {
        encontrarSolicitacaoParaServidor(
                solicitacaoId
        );

        return prepararDownload(
                solicitacaoId,
                anexoId
        );
    }

    private List<AnexoResponse> listarAnexos(
            Long solicitacaoId
    ) {
        return anexoRepository
                .findAllBySolicitacao_IdOrderByDataEnvioAsc(
                        solicitacaoId
                )
                .stream()
                .map(AnexoResponse::fromEntity)
                .toList();
    }

    private ArquivoDownload prepararDownload(
            Long solicitacaoId,
            Long anexoId
    ) {
        Anexo anexo = anexoRepository
                .findByIdAndSolicitacao_Id(
                        anexoId,
                        solicitacaoId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Anexo não encontrado"
                        )
                );

        return new ArquivoDownload(
                storageService.carregar(
                        anexo.getNomeArmazenado()
                ),
                anexo.getNomeOriginal(),
                anexo.getTipoConteudo(),
                anexo.getTamanho()
        );
    }

    private Solicitacao encontrarSolicitacaoDoCidadao(
            Long solicitacaoId,
            Long cidadaoId
    ) {
        return solicitacaoRepository
                .findByIdAndCidadao_Id(
                        solicitacaoId,
                        cidadaoId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Solicitação não encontrada"
                        )
                );
    }

    private Solicitacao encontrarSolicitacaoParaServidor(
            Long solicitacaoId
    ) {
        return solicitacaoRepository
                .buscarPorIdComDetalhes(solicitacaoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Solicitação não encontrada"
                        )
                );
    }

    private void validarStatusParaAnexo(
            Solicitacao solicitacao
    ) {
        StatusSolicitacao status =
                solicitacao.getStatus();

        if (status == StatusSolicitacao.CONCLUIDA
                || status == StatusSolicitacao.CANCELADA) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível anexar arquivos a uma solicitação encerrada"
            );
        }
    }
}