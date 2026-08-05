package br.com.fiap.enterprise_challenge3.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_ETP_ANEXO")
public class Anexo {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_etp_anexo"
    )
    @SequenceGenerator(
            name = "seq_etp_anexo",
            sequenceName = "SEQ_ETP_ANEXO",
            allocationSize = 1
    )
    @Column(name = "ID_ANEXO")
    private Long id;

    @Column(
            name = "NM_ARQUIVO_ORIGINAL",
            nullable = false,
            length = 255
    )
    private String nomeOriginal;

    @Column(
            name = "NM_ARQUIVO_ARMAZENADO",
            nullable = false,
            unique = true,
            length = 255
    )
    private String nomeArmazenado;

    @Column(
            name = "DS_TIPO_CONTEUDO",
            nullable = false,
            length = 100
    )
    private String tipoConteudo;

    @Column(
            name = "NR_TAMANHO",
            nullable = false
    )
    private Long tamanho;

    @Column(
            name = "DT_ENVIO",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ID_SOLICITACAO",
            nullable = false
    )
    private Solicitacao solicitacao;

    public Anexo() {
    }

    public Anexo(
            String nomeOriginal,
            String nomeArmazenado,
            String tipoConteudo,
            Long tamanho,
            Solicitacao solicitacao
    ) {
        this.nomeOriginal = nomeOriginal;
        this.nomeArmazenado = nomeArmazenado;
        this.tipoConteudo = tipoConteudo;
        this.tamanho = tamanho;
        this.solicitacao = solicitacao;
    }

    @PrePersist
    public void prepararEnvio() {
        if (dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public String getNomeArmazenado() {
        return nomeArmazenado;
    }

    public String getTipoConteudo() {
        return tipoConteudo;
    }

    public Long getTamanho() {
        return tamanho;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }
}