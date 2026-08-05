package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.SubservicoRequest;
import br.com.fiap.enterprise_challenge3.dto.SubservicoResponse;
import br.com.fiap.enterprise_challenge3.model.Categoria;
import br.com.fiap.enterprise_challenge3.model.Subservico;
import br.com.fiap.enterprise_challenge3.repository.CategoriaRepository;
import br.com.fiap.enterprise_challenge3.repository.SubservicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class SubservicoService {

    private final SubservicoRepository subservicoRepository;
    private final CategoriaRepository categoriaRepository;

    public SubservicoService(
            SubservicoRepository subservicoRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.subservicoRepository = subservicoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<SubservicoResponse> listarPorCategoria(
            Long categoriaId
    ) {
        encontrarCategoria(categoriaId);

        return subservicoRepository
                .findAllByCategoria_IdAndAtivoTrueOrderByNomeAsc(
                        categoriaId
                )
                .stream()
                .map(SubservicoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubservicoResponse buscarPorId(Long id) {
        return SubservicoResponse.fromEntity(
                encontrarSubservico(id)
        );
    }

    public SubservicoResponse cadastrar(
            SubservicoRequest request
    ) {
        Categoria categoria =
                encontrarCategoria(request.categoriaId());

        validarCategoriaAtiva(categoria);

        String nome = request.nome().trim();

        validarDuplicidade(
                nome,
                categoria.getId()
        );

        Subservico subservico = new Subservico(
                nome,
                normalizarDescricao(request.descricao()),
                categoria
        );

        return SubservicoResponse.fromEntity(
                subservicoRepository.save(subservico)
        );
    }

    public SubservicoResponse atualizar(
            Long id,
            SubservicoRequest request
    ) {
        Subservico subservico =
                encontrarSubservico(id);

        Categoria categoria =
                encontrarCategoria(request.categoriaId());

        validarCategoriaAtiva(categoria);

        String nome = request.nome().trim();

        boolean nomeOuCategoriaAlterados =
                !subservico.getNome().equalsIgnoreCase(nome)
                        || !subservico.getCategoria()
                        .getId()
                        .equals(categoria.getId());

        if (nomeOuCategoriaAlterados) {
            validarDuplicidade(nome, categoria.getId());
        }

        subservico.setNome(nome);
        subservico.setDescricao(
                normalizarDescricao(request.descricao())
        );
        subservico.setCategoria(categoria);

        return SubservicoResponse.fromEntity(
                subservicoRepository.save(subservico)
        );
    }

    public void desativar(Long id) {
        Subservico subservico =
                encontrarSubservico(id);

        subservico.setAtivo(false);

        subservicoRepository.save(subservico);
    }

    private Categoria encontrarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Categoria não encontrada"
                        )
                );
    }

    private Subservico encontrarSubservico(Long id) {
        return subservicoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Subserviço não encontrado"
                        )
                );
    }

    private void validarCategoriaAtiva(
            Categoria categoria
    ) {
        if (!Boolean.TRUE.equals(categoria.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível utilizar uma categoria desativada"
            );
        }
    }

    private void validarDuplicidade(
            String nome,
            Long categoriaId
    ) {
        if (subservicoRepository
                .existsByNomeIgnoreCaseAndCategoria_Id(
                        nome,
                        categoriaId
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe esse subserviço na categoria"
            );
        }
    }

    private String normalizarDescricao(
            String descricao
    ) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }

        return descricao.trim();
    }
}