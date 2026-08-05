package br.com.fiap.enterprise_challenge3.model;

import br.com.fiap.enterprise_challenge3.model.enums.StatusSolicitacao;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_ETP_HISTORICO_SOLICITACAO")
public class HistoricoSolicitacao {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_etp_hist_solicitacao"
    )
    @SequenceGenerator(
            name = "seq_etp_hist_solicitacao",
            sequenceName = "SEQ_ETP_HIST_SOLICITACAO",
            allocationSize = 1
    )
    @Column(name = "ID_HISTORICO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_SOLICITACAO",
            nullable = false
    )
    private Solicitacao solicitacao;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "ST_ANTERIOR",
            length = 30
    )
    private StatusSolicitacao statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "ST_NOVO",
            nullable = false,
            length = 30
    )
    private StatusSolicitacao statusNovo;

    @Column(
            name = "DS_OBSERVACAO",
            length = 500
    )
    private String observacao;

    @Column(
            name = "DT_ALTERACAO",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataAlteracao;

    public HistoricoSolicitacao() {
    }

    public HistoricoSolicitacao(
            Solicitacao solicitacao,
            StatusSolicitacao statusAnterior,
            StatusSolicitacao statusNovo,
            String observacao
    ) {
        this.solicitacao = solicitacao;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.observacao = observacao;
    }

    @PrePersist
    public void prepararCadastro() {
        if (dataAlteracao == null) {
            dataAlteracao = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public StatusSolicitacao getStatusAnterior() {
        return statusAnterior;
    }

    public StatusSolicitacao getStatusNovo() {
        return statusNovo;
    }

    public String getObservacao() {
        return observacao;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }
}