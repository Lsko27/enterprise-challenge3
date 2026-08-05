package br.com.fiap.enterprise_challenge3.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_ETP_SERVIDOR")
public class Servidor {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_etp_servidor"
    )
    @SequenceGenerator(
            name = "seq_etp_servidor",
            sequenceName = "SEQ_ETP_SERVIDOR",
            allocationSize = 1
    )
    @Column(name = "ID_SERVIDOR")
    private Long id;

    @Column(
            name = "NM_SERVIDOR",
            nullable = false,
            length = 150
    )
    private String nome;

    @Column(
            name = "NR_MATRICULA",
            nullable = false,
            unique = true,
            length = 30
    )
    private String matricula;

    @Column(
            name = "DS_EMAIL",
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    @Column(
            name = "DS_SENHA",
            nullable = false,
            length = 255
    )
    private String senha;

    @Column(name = "DS_CARGO", length = 100)
    private String cargo;

    @Column(
            name = "DT_CADASTRO",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataCadastro;

    @Column(name = "ATIVO", nullable = false)
    private Boolean ativo = true;

    public Servidor() {
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

    public String getNome() {
        return nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getCargo() {
        return cargo;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public Boolean getAtivo() {
        return ativo;
    }
}