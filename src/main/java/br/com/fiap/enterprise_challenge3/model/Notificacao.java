package br.com.fiap.enterprise_challenge3.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_ETP_NOTIFICACAO")
public class Notificacao {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_etp_notificacao"
    )
    @SequenceGenerator(
            name = "seq_etp_notificacao",
            sequenceName = "SEQ_ETP_NOTIFICACAO",
            allocationSize = 1
    )
    @Column(name = "ID_NOTIFICACAO")
    private Long id;

    @Column(
            name = "DS_TITULO",
            nullable = false,
            length = 150
    )
    private String titulo;

    @Column(
            name = "DS_MENSAGEM",
            nullable = false,
            length = 1000
    )
    private String mensagem;

    @Column(name = "LIDA", nullable = false)
    private Boolean lida = false;

    @Column(
            name = "DT_CRIACAO",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataCriacao;

    @Column(name = "DT_LEITURA")
    private LocalDateTime dataLeitura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ID_CIDADAO",
            nullable = false
    )
    private Cidadao cidadao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ID_SOLICITACAO",
            nullable = false
    )
    private Solicitacao solicitacao;

    public Notificacao() {
    }

    public Notificacao(
            String titulo,
            String mensagem,
            Cidadao cidadao,
            Solicitacao solicitacao
    ) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.cidadao = cidadao;
        this.solicitacao = solicitacao;
        this.lida = false;
    }

    @PrePersist
    public void prepararCriacao() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }

        if (lida == null) {
            lida = false;
        }
    }

    public void marcarComoLida() {
        if (!Boolean.TRUE.equals(lida)) {
            this.lida = true;
            this.dataLeitura = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Boolean getLida() {
        return lida;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataLeitura() {
        return dataLeitura;
    }

    public Cidadao getCidadao() {
        return cidadao;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }
}