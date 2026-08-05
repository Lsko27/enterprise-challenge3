package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.model.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnexoRepository
        extends JpaRepository<Anexo, Long> {

    List<Anexo>
    findAllBySolicitacao_IdOrderByDataEnvioAsc(
            Long solicitacaoId
    );

    Optional<Anexo> findByIdAndSolicitacao_Id(
            Long anexoId,
            Long solicitacaoId
    );

    long countBySolicitacao_Id(
            Long solicitacaoId
    );
}