package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.CategoriaRequest;
import br.com.fiap.enterprise_challenge3.dto.CategoriaResponse;
import br.com.fiap.enterprise_challenge3.model.Categoria;
import br.com.fiap.enterprise_challenge3.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listarAtivas() {
        return categoriaRepository
                .findAllByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(CategoriaResponse::fromEntity)
                .toList();
    }

    public CategoriaResponse buscarPorId(Long id) {
        return CategoriaResponse.fromEntity(encontrarCategoria(id));
    }

    public CategoriaResponse cadastrar(CategoriaRequest request) {
        String nome = request.nome().trim();

        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma categoria com esse nome"
            );
        }

        Categoria categoria = new Categoria(
                nome,
                normalizarDescricao(request.descricao())
        );

        Categoria categoriaSalva = categoriaRepository.save(categoria);

        return CategoriaResponse.fromEntity(categoriaSalva);
    }

    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = encontrarCategoria(id);
        String nome = request.nome().trim();

        boolean nomeFoiAlterado =
                !categoria.getNome().equalsIgnoreCase(nome);

        if (nomeFoiAlterado &&
                categoriaRepository.existsByNomeIgnoreCase(nome)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma categoria com esse nome"
            );
        }

        categoria.setNome(nome);
        categoria.setDescricao(normalizarDescricao(request.descricao()));

        Categoria categoriaAtualizada =
                categoriaRepository.save(categoria);

        return CategoriaResponse.fromEntity(categoriaAtualizada);
    }

    public void desativar(Long id) {
        Categoria categoria = encontrarCategoria(id);
        categoria.setAtivo(false);
        categoriaRepository.save(categoria);
    }

    private Categoria encontrarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria não encontrada"
                ));
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }

        return descricao.trim();
    }
}