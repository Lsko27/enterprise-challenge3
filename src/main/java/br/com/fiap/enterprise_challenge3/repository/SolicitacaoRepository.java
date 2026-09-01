package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.model.Solicitacao;
import br.com.fiap.enterprise_challenge3.model.enums.StatusSolicitacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SolicitacaoRepository
        extends JpaRepository<Solicitacao, Long> {

    @EntityGraph(attributePaths = {
            "subservico",
            "subservico.categoria",
            "endereco"
    })
    List<Solicitacao>
    findAllByCidadao_IdOrderByDataAberturaDesc(
            Long cidadaoId
    );

    @EntityGraph(attributePaths = {
            "subservico",
            "subservico.categoria",
            "endereco"
    })
    Optional<Solicitacao> findByIdAndCidadao_Id(
            Long id,
            Long cidadaoId
    );

    @EntityGraph(attributePaths = {
            "cidadao",
            "subservico",
            "subservico.categoria",
            "endereco"
    })
    @Query("""
            SELECT solicitacao
            FROM Solicitacao solicitacao
            ORDER BY solicitacao.dataAbertura DESC
            """)
    List<Solicitacao> listarTodasComDetalhes();

    @EntityGraph(attributePaths = {
            "cidadao",
            "subservico",
            "subservico.categoria",
            "endereco"
    })
    List<Solicitacao> findAllByStatusIn(
            Collection<StatusSolicitacao> status
    );

    @EntityGraph(attributePaths = {
            "cidadao",
            "subservico",
            "subservico.categoria",
            "endereco"
    })
    @Query("""
            SELECT solicitacao
            FROM Solicitacao solicitacao
            WHERE solicitacao.id = :id
            """)
    Optional<Solicitacao> buscarPorIdComDetalhes(
            @Param("id") Long id
    );
}