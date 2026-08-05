package br.com.fiap.enterprise_challenge3.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_ETP_CIDADAO")
public class Cidadao {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_etp_cidadao"
    )
    @SequenceGenerator(
            name = "seq_etp_cidadao",
            sequenceName = "SEQ_ETP_CIDADAO",
            allocationSize = 1
    )
    @Column(name = "ID_CIDADAO")
    private Long id;

    @Column(
            name = "NM_CIDADAO",
            nullable = false,
            length = 150
    )
    private String nome;

    @Column(
            name = "NR_CPF",
            nullable = false,
            unique = true,
            length = 11
    )
    private String cpf;

    @Column(
            name = "DS_EMAIL",
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    @Column(name = "NR_TELEFONE", length = 20)
    private String telefone;

    @Column(
            name = "DS_SENHA",
            nullable = false,
            length = 255
    )
    private String senha;

    @Column(
            name = "DT_CADASTRO",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataCadastro;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;

    public Cidadao() {
    }

    public Cidadao(
            String nome,
            String cpf,
            String email,
            String telefone,
            String senha
    ) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.senha = senha;
        this.ativo = true;
    }

    @PrePersist
    public void prepararCadastro() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }

        if (ativo == null) {
            ativo = true;
        }
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}