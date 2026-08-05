package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository
        extends JpaRepository<Notificacao, Long> {

    List<Notificacao>
    findAllByCidadao_IdOrderByDataCriacaoDesc(
            Long cidadaoId
    );

    Optional<Notificacao> findByIdAndCidadao_Id(
            Long notificacaoId,
            Long cidadaoId
    );

    long countByCidadao_IdAndLidaFalse(
            Long cidadaoId
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE Notificacao notificacao
            SET notificacao.lida = true,
                notificacao.dataLeitura = :dataLeitura
            WHERE notificacao.cidadao.id = :cidadaoId
              AND notificacao.lida = false
            """)
    int marcarTodasComoLidas(
            @Param("cidadaoId") Long cidadaoId,
            @Param("dataLeitura") LocalDateTime dataLeitura
    );
}