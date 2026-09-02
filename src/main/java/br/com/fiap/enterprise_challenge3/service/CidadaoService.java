package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.CidadaoCreateRequest;
import br.com.fiap.enterprise_challenge3.dto.CidadaoResponse;
import br.com.fiap.enterprise_challenge3.dto.CidadaoUpdateRequest;
import br.com.fiap.enterprise_challenge3.dto.auditoria.ContextoAuditoria;
import br.com.fiap.enterprise_challenge3.model.Cidadao;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import br.com.fiap.enterprise_challenge3.repository.CidadaoRepository;
import br.com.fiap.enterprise_challenge3.util.CpfValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class CidadaoService {

    private final CidadaoRepository cidadaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistroAuditoriaService auditoriaService;

    public CidadaoService(
            CidadaoRepository cidadaoRepository,
            PasswordEncoder passwordEncoder,
            RegistroAuditoriaService auditoriaService
    ) {
        this.cidadaoRepository = cidadaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<CidadaoResponse> listarAtivos() {
        return cidadaoRepository
                .findAllByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(CidadaoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CidadaoResponse buscarPorId(
            Long id,
            ContextoAuditoria contextoAuditoria
    ) {
        CidadaoResponse resposta =
                CidadaoResponse.fromEntity(
                        encontrarCidadao(id)
                );

        auditoriaService.registrar(
                TipoAtorAuditoria.CIDADAO,
                id,
                AcaoAuditoria.CONSULTA_DADOS_PESSOAIS,
                TipoRecursoAuditoria.CIDADAO,
                id.toString(),
                null,
                null,
                ResultadoAuditoria.SUCESSO,
                contextoAuditoria,
                "Cidadão consultou os dados do próprio perfil"
        );

        return resposta;
    }

    public CidadaoResponse cadastrar(
            CidadaoCreateRequest request
    ) {
        String cpf =
                normalizarCpf(request.cpf());

        String email =
                normalizarEmail(request.email());

        validarCpf(cpf);
        validarCpfDuplicado(cpf);
        validarEmailDuplicado(email);

        Cidadao cidadao =
                new Cidadao(
                        request.nome().trim(),
                        cpf,
                        email,
                        normalizarTelefone(
                                request.telefone()
                        ),
                        passwordEncoder.encode(
                                request.senha()
                        )
                );

        return CidadaoResponse.fromEntity(
                cidadaoRepository.save(cidadao)
        );
    }

    public CidadaoResponse atualizar(
            Long id,
            CidadaoUpdateRequest request,
            ContextoAuditoria contextoAuditoria
    ) {
        Cidadao cidadao =
                encontrarCidadao(id);

        String novoNome =
                request.nome().trim();

        String novoEmail =
                normalizarEmail(request.email());

        String novoTelefone =
                normalizarTelefone(
                        request.telefone()
                );

        if (
                cidadaoRepository
                        .existsByEmailIgnoreCaseAndIdNot(
                                novoEmail,
                                id
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O e-mail informado já está cadastrado"
            );
        }

        List<String> camposAlterados =
                identificarCamposAlterados(
                        cidadao,
                        novoNome,
                        novoEmail,
                        novoTelefone
                );

        cidadao.setNome(novoNome);
        cidadao.setEmail(novoEmail);
        cidadao.setTelefone(novoTelefone);

        Cidadao cidadaoSalvo =
                cidadaoRepository.saveAndFlush(
                        cidadao
                );

        String detalhe =
                camposAlterados.isEmpty()
                        ? "Atualização solicitada sem alteração efetiva"
                        : "Campos alterados no perfil: "
                        + String.join(
                        ", ",
                        camposAlterados
                );

        auditoriaService.registrar(
                TipoAtorAuditoria.CIDADAO,
                id,
                AcaoAuditoria.ATUALIZACAO_PERFIL,
                TipoRecursoAuditoria.CIDADAO,
                id.toString(),
                null,
                null,
                ResultadoAuditoria.SUCESSO,
                contextoAuditoria,
                detalhe
        );

        return CidadaoResponse.fromEntity(
                cidadaoSalvo
        );
    }

    public void desativar(
            Long id,
            ContextoAuditoria contextoAuditoria
    ) {
        Cidadao cidadao =
                encontrarCidadao(id);

        if (!Boolean.TRUE.equals(cidadao.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A conta já está desativada"
            );
        }

        cidadao.setAtivo(false);

        cidadaoRepository.saveAndFlush(
                cidadao
        );

        auditoriaService.registrar(
                TipoAtorAuditoria.CIDADAO,
                id,
                AcaoAuditoria.DESATIVACAO_CIDADAO,
                TipoRecursoAuditoria.CIDADAO,
                id.toString(),
                "ATIVO",
                "INATIVO",
                ResultadoAuditoria.SUCESSO,
                contextoAuditoria,
                "Cidadão desativou a própria conta"
        );
    }

    private List<String> identificarCamposAlterados(
            Cidadao cidadao,
            String novoNome,
            String novoEmail,
            String novoTelefone
    ) {
        List<String> campos =
                new ArrayList<>();

        if (
                !Objects.equals(
                        cidadao.getNome(),
                        novoNome
                )
        ) {
            campos.add("nome");
        }

        if (
                !Objects.equals(
                        cidadao.getEmail(),
                        novoEmail
                )
        ) {
            campos.add("e-mail");
        }

        if (
                !Objects.equals(
                        cidadao.getTelefone(),
                        novoTelefone
                )
        ) {
            campos.add("telefone");
        }

        return List.copyOf(campos);
    }

    private Cidadao encontrarCidadao(
            Long id
    ) {
        return cidadaoRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Cidadão não encontrado"
                        )
                );
    }

    private void validarCpf(
            String cpf
    ) {
        if (!CpfValidator.isValid(cpf)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O CPF informado é inválido"
            );
        }
    }

    private void validarCpfDuplicado(
            String cpf
    ) {
        if (cidadaoRepository.existsByCpf(cpf)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O CPF informado já está cadastrado"
            );
        }
    }

    private void validarEmailDuplicado(
            String email
    ) {
        if (
                cidadaoRepository
                        .existsByEmailIgnoreCase(email)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O e-mail informado já está cadastrado"
            );
        }
    }

    private String normalizarCpf(
            String cpf
    ) {
        return cpf.replaceAll(
                "\\D",
                ""
        );
    }

    private String normalizarEmail(
            String email
    ) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizarTelefone(
            String telefone
    ) {
        if (
                telefone == null
                        || telefone.isBlank()
        ) {
            return null;
        }

        String numeros =
                telefone.replaceAll(
                        "\\D",
                        ""
                );

        if (
                numeros.length() < 10
                        || numeros.length() > 11
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O telefone deve possuir 10 ou 11 dígitos"
            );
        }

        return numeros;
    }
}