package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.model.AnaliseIa;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnaliseIaRepository
        extends JpaRepository<AnaliseIa, Long> {

    @EntityGraph(attributePaths = {
            "solicitacao",
            "categoriaSugerida",
            "subservicoSugerido"
    })
    List<AnaliseIa>
    findBySolicitacao_IdOrderByDataAnaliseDesc(
            Long solicitacaoId
    );

    @EntityGraph(attributePaths = {
            "solicitacao",
            "categoriaSugerida",
            "subservicoSugerido"
    })
    Optional<AnaliseIa>
    findFirstBySolicitacao_IdOrderByDataAnaliseDesc(
            Long solicitacaoId
    );

    boolean existsBySolicitacao_Id(
            Long solicitacaoId
    );
}