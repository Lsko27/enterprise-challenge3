package br.com.fiap.enterprise_challenge3.model;

import br.com.fiap.enterprise_challenge3.model.enums.NivelUrgencia;
import br.com.fiap.enterprise_challenge3.model.enums.StatusSolicitacao;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_ETP_SOLICITACAO")
public class Solicitacao {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_etp_solicitacao"
    )
    @SequenceGenerator(
            name = "seq_etp_solicitacao",
            sequenceName = "SEQ_ETP_SOLICITACAO",
            allocationSize = 1
    )
    @Column(name = "ID_SOLICITACAO")
    private Long id;

    @Column(
            name = "DS_TITULO",
            nullable = false,
            length = 150
    )
    private String titulo;

    @Column(
            name = "DS_SOLICITACAO",
            nullable = false,
            length = 2000
    )
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "ST_SOLICITACAO",
            nullable = false,
            length = 30
    )
    private StatusSolicitacao status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "NV_URGENCIA",
            nullable = false,
            length = 20
    )
    private NivelUrgencia urgencia;

    @Column(
            name = "DT_ABERTURA",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataAbertura;

    @Column(name = "DT_ATUALIZACAO")
    private LocalDateTime dataAtualizacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CIDADAO", nullable = false)
    private Cidadao cidadao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SUBSERVICO", nullable = false)
    private Subservico subservico;

    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            optional = false
    )
    @JoinColumn(
            name = "ID_ENDERECO",
            nullable = false,
            unique = true
    )
    private Endereco endereco;

    public Solicitacao() {
    }

    public Solicitacao(
            String titulo,
            String descricao,
            NivelUrgencia urgencia,
            Cidadao cidadao,
            Subservico subservico,
            Endereco endereco
    ) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.urgencia = urgencia;
        this.cidadao = cidadao;
        this.subservico = subservico;
        this.endereco = endereco;
        this.status = StatusSolicitacao.REGISTRADA;
    }

    @PrePersist
    public void prepararCadastro() {
        dataAbertura = LocalDateTime.now();
        dataAtualizacao = dataAbertura;

        if (status == null) {
            status = StatusSolicitacao.REGISTRADA;
        }
    }

    @PreUpdate
    public void prepararAtualizacao() {
        dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }

    public NivelUrgencia getUrgencia() {
        return urgencia;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public Cidadao getCidadao() {
        return cidadao;
    }

    public Subservico getSubservico() {
        return subservico;
    }

    public Endereco getEndereco() {
        return endereco;
    }
}