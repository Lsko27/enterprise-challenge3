package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.CidadaoCreateRequest;
import br.com.fiap.enterprise_challenge3.dto.CidadaoResponse;
import br.com.fiap.enterprise_challenge3.dto.CidadaoUpdateRequest;
import br.com.fiap.enterprise_challenge3.model.Cidadao;
import br.com.fiap.enterprise_challenge3.repository.CidadaoRepository;
import br.com.fiap.enterprise_challenge3.util.CpfValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class CidadaoService {

    private final CidadaoRepository cidadaoRepository;
    private final PasswordEncoder passwordEncoder;

    public CidadaoService(
            CidadaoRepository cidadaoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.cidadaoRepository = cidadaoRepository;
        this.passwordEncoder = passwordEncoder;
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
    public CidadaoResponse buscarPorId(Long id) {
        return CidadaoResponse.fromEntity(
                encontrarCidadao(id)
        );
    }

    public CidadaoResponse cadastrar(
            CidadaoCreateRequest request
    ) {
        String cpf = normalizarCpf(request.cpf());
        String email = normalizarEmail(request.email());

        validarCpf(cpf);
        validarCpfDuplicado(cpf);
        validarEmailDuplicado(email);

        Cidadao cidadao = new Cidadao(
                request.nome().trim(),
                cpf,
                email,
                normalizarTelefone(request.telefone()),
                passwordEncoder.encode(request.senha())
        );

        return CidadaoResponse.fromEntity(
                cidadaoRepository.save(cidadao)
        );
    }

    public CidadaoResponse atualizar(
            Long id,
            CidadaoUpdateRequest request
    ) {
        Cidadao cidadao = encontrarCidadao(id);
        String email = normalizarEmail(request.email());

        if (cidadaoRepository
                .existsByEmailIgnoreCaseAndIdNot(email, id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O e-mail informado já está cadastrado"
            );
        }

        cidadao.setNome(request.nome().trim());
        cidadao.setEmail(email);
        cidadao.setTelefone(
                normalizarTelefone(request.telefone())
        );

        return CidadaoResponse.fromEntity(
                cidadaoRepository.save(cidadao)
        );
    }

    public void desativar(Long id) {
        Cidadao cidadao = encontrarCidadao(id);
        cidadao.setAtivo(false);
        cidadaoRepository.save(cidadao);
    }

    private Cidadao encontrarCidadao(Long id) {
        return cidadaoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Cidadão não encontrado"
                        )
                );
    }

    private void validarCpf(String cpf) {
        if (!CpfValidator.isValid(cpf)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O CPF informado é inválido"
            );
        }
    }

    private void validarCpfDuplicado(String cpf) {
        if (cidadaoRepository.existsByCpf(cpf)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O CPF informado já está cadastrado"
            );
        }
    }

    private void validarEmailDuplicado(String email) {
        if (cidadaoRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O e-mail informado já está cadastrado"
            );
        }
    }

    private String normalizarCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }

    private String normalizarEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return null;
        }

        String numeros = telefone.replaceAll("\\D", "");

        if (numeros.length() < 10 || numeros.length() > 11) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O telefone deve possuir 10 ou 11 dígitos"
            );
        }

        return numeros;
    }
}