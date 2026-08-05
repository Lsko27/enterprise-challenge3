package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.model.HistoricoSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoSolicitacaoRepository
        extends JpaRepository<HistoricoSolicitacao, Long> {

    List<HistoricoSolicitacao>
    findAllBySolicitacao_IdOrderByDataAlteracaoAsc(
            Long solicitacaoId
    );
}