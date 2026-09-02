package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.model.RegistroAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RegistroAuditoriaRepository
        extends Repository<RegistroAuditoria, Long> {

    RegistroAuditoria save(
            RegistroAuditoria registro
    );

    @Query("""
            SELECT registro
            FROM RegistroAuditoria registro
            WHERE (
                :tipoAtor IS NULL
                OR registro.tipoAtor = :tipoAtor
            )
            AND (
                :atorId IS NULL
                OR registro.atorId = :atorId
            )
            AND (
                :acao IS NULL
                OR registro.acao = :acao
            )
            AND (
                :tipoRecurso IS NULL
                OR registro.tipoRecurso = :tipoRecurso
            )
            AND (
                :recursoId IS NULL
                OR registro.recursoId = :recursoId
            )
            AND (
                :resultado IS NULL
                OR registro.resultado = :resultado
            )
            AND (
                :inicio IS NULL
                OR registro.dataEvento >= :inicio
            )
            AND (
                :fimExclusivo IS NULL
                OR registro.dataEvento < :fimExclusivo
            )
            """)
    Page<RegistroAuditoria> consultar(
            @Param("tipoAtor")
            TipoAtorAuditoria tipoAtor,

            @Param("atorId")
            Long atorId,

            @Param("acao")
            AcaoAuditoria acao,

            @Param("tipoRecurso")
            TipoRecursoAuditoria tipoRecurso,

            @Param("recursoId")
            String recursoId,

            @Param("resultado")
            ResultadoAuditoria resultado,

            @Param("inicio")
            LocalDateTime inicio,

            @Param("fimExclusivo")
            LocalDateTime fimExclusivo,

            Pageable pageable
    );
}