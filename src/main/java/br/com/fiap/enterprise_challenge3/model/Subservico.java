package br.com.fiap.enterprise_challenge3.model;

import jakarta.persistence.*;

@Entity
@Table(name = "T_ETP_SUBSERVICO")
public class Subservico {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_etp_subservico"
    )
    @SequenceGenerator(
            name = "seq_etp_subservico",
            sequenceName = "SEQ_ETP_SUBSERVICO",
            allocationSize = 1
    )
    @Column(name = "ID_SUBSERVICO")
    private Long id;

    @Column(
            name = "NM_SUBSERVICO",
            nullable = false,
            length = 100
    )
    private String nome;

    @Column(name = "DS_SUBSERVICO", length = 255)
    private String descricao;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CATEGORIA", nullable = false)
    private Categoria categoria;

    public Subservico() {
    }

    public Subservico(
            String nome,
            String descricao,
            Categoria categoria
    ) {
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}