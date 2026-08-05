package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.model.Subservico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubservicoRepository
        extends JpaRepository<Subservico, Long> {

    boolean existsByNomeIgnoreCaseAndCategoria_Id(
            String nome,
            Long categoriaId
    );

    List<Subservico>
    findAllByCategoria_IdAndAtivoTrueOrderByNomeAsc(
            Long categoriaId
    );
}