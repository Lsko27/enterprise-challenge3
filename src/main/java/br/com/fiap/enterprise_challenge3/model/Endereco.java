package br.com.fiap.enterprise_challenge3.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "T_ETP_ENDERECO")
public class Endereco {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_etp_endereco"
    )
    @SequenceGenerator(
            name = "seq_etp_endereco",
            sequenceName = "SEQ_ETP_ENDERECO",
            allocationSize = 1
    )
    @Column(name = "ID_ENDERECO")
    private Long id;

    @Column(
            name = "DS_LOGRADOURO",
            nullable = false,
            length = 150
    )
    private String logradouro;

    @Column(name = "NR_ENDERECO", length = 20)
    private String numero;

    @Column(name = "DS_COMPLEMENTO", length = 100)
    private String complemento;

    @Column(
            name = "NM_BAIRRO",
            nullable = false,
            length = 100
    )
    private String bairro;

    @Column(
            name = "NM_CIDADE",
            nullable = false,
            length = 100
    )
    private String cidade;

    @Column(
            name = "SG_ESTADO",
            nullable = false,
            length = 2
    )
    private String estado;

    @Column(name = "NR_CEP", length = 8)
    private String cep;

    @Column(
            name = "NR_LATITUDE",
            precision = 10,
            scale = 7
    )
    private BigDecimal latitude;

    @Column(
            name = "NR_LONGITUDE",
            precision = 10,
            scale = 7
    )
    private BigDecimal longitude;

    public Endereco() {
    }

    public Endereco(
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String estado,
            String cep,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() {
        return id;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getEstado() {
        return estado;
    }

    public String getCep() {
        return cep;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }
}