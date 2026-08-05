package br.com.fiap.enterprise_challenge3.model;

import jakarta.persistence.*;

@Entity
@Table(name = "T_ETP_CATEGORIA")
public class Categoria {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_etp_categoria"
    )
    @SequenceGenerator(
            name = "seq_etp_categoria",
            sequenceName = "SEQ_ETP_CATEGORIA",
            allocationSize = 1
    )
    @Column(name = "ID_CATEGORIA")
    private Long id;

    @Column(
            name = "NM_CATEGORIA",
            nullable = false,
            unique = true,
            length = 100
    )
    private String nome;

    @Column(name = "DS_CATEGORIA", length = 255)
    private String descricao;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;

    public Categoria() {
    }

    public Categoria(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}