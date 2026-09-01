package br.com.fiap.enterprise_challenge3.model;

import br.com.fiap.enterprise_challenge3.model.enums.PerfilServidor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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

    @Column(
            name = "DS_CARGO",
            length = 100
    )
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "DS_PERFIL",
            nullable = false,
            length = 30
    )
    private PerfilServidor perfil =
            PerfilServidor.SERVIDOR;

    @Column(
            name = "DT_CADASTRO",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataCadastro;

    @Column(
            name = "ATIVO",
            nullable = false
    )
    private Boolean ativo = true;

    public Servidor() {
    }

    public Servidor(
            String nome,
            String matricula,
            String email,
            String senha,
            String cargo,
            PerfilServidor perfil
    ) {
        this.nome = nome;
        this.matricula = matricula;
        this.email = email;
        this.senha = senha;
        this.cargo = cargo;
        this.perfil = perfil;
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

        if (perfil == null) {
            perfil = PerfilServidor.SERVIDOR;
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

    public PerfilServidor getPerfil() {
        return perfil;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public Boolean getAtivo() {
        return ativo;
    }
}