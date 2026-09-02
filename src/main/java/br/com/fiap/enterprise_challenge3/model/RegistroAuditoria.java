package br.com.fiap.enterprise_challenge3.model;

import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Immutable
@Table(name = "T_ETP_REGISTRO_AUDITORIA")
public class RegistroAuditoria {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_etp_reg_auditoria"
    )
    @SequenceGenerator(
            name = "seq_etp_reg_auditoria",
            sequenceName = "SEQ_ETP_REG_AUDITORIA",
            allocationSize = 1
    )
    @Column(
            name = "ID_AUDITORIA",
            updatable = false
    )
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "TP_PERFIL_ATOR",
            nullable = false,
            updatable = false,
            length = 20
    )
    private TipoAtorAuditoria tipoAtor;

    @Column(
            name = "ID_ATOR",
            updatable = false
    )
    private Long atorId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "TP_ACAO",
            nullable = false,
            updatable = false,
            length = 50
    )
    private AcaoAuditoria acao;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "TP_RECURSO",
            nullable = false,
            updatable = false,
            length = 30
    )
    private TipoRecursoAuditoria tipoRecurso;

    @Column(
            name = "ID_RECURSO",
            updatable = false,
            length = 100
    )
    private String recursoId;

    @Column(
            name = "VL_ANTERIOR",
            updatable = false,
            length = 200
    )
    private String valorAnterior;

    @Column(
            name = "VL_NOVO",
            updatable = false,
            length = 200
    )
    private String valorNovo;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "TP_RESULTADO",
            nullable = false,
            updatable = false,
            length = 20
    )
    private ResultadoAuditoria resultado;

    @Column(
            name = "DS_METODO_HTTP",
            updatable = false,
            length = 10
    )
    private String metodoHttp;

    @Column(
            name = "DS_ENDPOINT",
            updatable = false,
            length = 300
    )
    private String endpoint;

    @Column(
            name = "DS_IP_ORIGEM",
            updatable = false,
            length = 64
    )
    private String enderecoIp;

    @Column(
            name = "DS_DETALHE",
            updatable = false,
            length = 1000
    )
    private String detalhe;

    @Column(
            name = "DT_EVENTO",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataEvento;

    public RegistroAuditoria() {
    }

    public RegistroAuditoria(
            TipoAtorAuditoria tipoAtor,
            Long atorId,
            AcaoAuditoria acao,
            TipoRecursoAuditoria tipoRecurso,
            String recursoId,
            String valorAnterior,
            String valorNovo,
            ResultadoAuditoria resultado,
            String metodoHttp,
            String endpoint,
            String enderecoIp,
            String detalhe
    ) {
        this.tipoAtor = Objects.requireNonNull(
                tipoAtor,
                "O tipo do ator é obrigatório"
        );

        this.atorId = atorId;

        this.acao = Objects.requireNonNull(
                acao,
                "A ação de auditoria é obrigatória"
        );

        this.tipoRecurso = Objects.requireNonNull(
                tipoRecurso,
                "O tipo do recurso é obrigatório"
        );

        this.recursoId = normalizar(
                recursoId,
                100
        );

        this.valorAnterior = normalizar(
                valorAnterior,
                200
        );

        this.valorNovo = normalizar(
                valorNovo,
                200
        );

        this.resultado = Objects.requireNonNull(
                resultado,
                "O resultado é obrigatório"
        );

        this.metodoHttp = normalizar(
                metodoHttp,
                10
        );

        this.endpoint = normalizar(
                endpoint,
                300
        );

        this.enderecoIp = normalizar(
                enderecoIp,
                64
        );

        this.detalhe = normalizar(
                detalhe,
                1000
        );
    }

    @PrePersist
    public void prepararCadastro() {
        if (dataEvento == null) {
            dataEvento = LocalDateTime.now();
        }
    }

    private String normalizar(
            String valor,
            int tamanhoMaximo
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String valorNormalizado = valor.trim();

        if (valorNormalizado.length() <= tamanhoMaximo) {
            return valorNormalizado;
        }

        return valorNormalizado.substring(
                0,
                tamanhoMaximo
        );
    }

    public Long getId() {
        return id;
    }

    public TipoAtorAuditoria getTipoAtor() {
        return tipoAtor;
    }

    public Long getAtorId() {
        return atorId;
    }

    public AcaoAuditoria getAcao() {
        return acao;
    }

    public TipoRecursoAuditoria getTipoRecurso() {
        return tipoRecurso;
    }

    public String getRecursoId() {
        return recursoId;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public String getValorNovo() {
        return valorNovo;
    }

    public ResultadoAuditoria getResultado() {
        return resultado;
    }

    public String getMetodoHttp() {
        return metodoHttp;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getEnderecoIp() {
        return enderecoIp;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public LocalDateTime getDataEvento() {
        return dataEvento;
    }
}