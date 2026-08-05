package br.com.fiap.enterprise_challenge3.model;

import br.com.fiap.enterprise_challenge3.converter.BooleanToNumberConverter;
import br.com.fiap.enterprise_challenge3.model.enums.NivelPrioridade;
import br.com.fiap.enterprise_challenge3.model.enums.NivelUrgencia;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_ETP_ANALISE_IA")
public class AnaliseIa {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "analise_ia_sequence"
    )
    @SequenceGenerator(
            name = "analise_ia_sequence",
            sequenceName = "SQ_ETP_ANALISE_IA",
            allocationSize = 1
    )
    @Column(name = "id_analise")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_solicitacao",
            nullable = false
    )
    private Solicitacao solicitacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_sugerida")
    private Categoria categoriaSugerida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subservico_sugerido")
    private Subservico subservicoSugerido;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "ds_urgencia_sugerida",
            length = 30
    )
    private NivelUrgencia urgenciaSugerida;

    @Column(
            name = "nr_score_prioridade",
            precision = 5,
            scale = 2
    )
    private BigDecimal scorePrioridade;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "ds_nivel_prioridade",
            length = 30
    )
    private NivelPrioridade nivelPrioridade;

    @Column(
            name = "nr_confianca",
            precision = 5,
            scale = 4
    )
    private BigDecimal confianca;

    @Column(
            name = "ds_justificativa",
            length = 1000
    )
    private String justificativa;

    @Column(
            name = "nm_modelo",
            length = 100
    )
    private String nomeModelo;

    @Column(
            name = "ds_versao_modelo",
            length = 50
    )
    private String versaoModelo;

    @Convert(converter = BooleanToNumberConverter.class)
    @Column(
            name = "fl_aceita_cidadao",
            nullable = false
    )
    private Boolean aceitaCidadao = false;

    @Column(
            name = "dt_analise",
            nullable = false
    )
    private LocalDateTime dataAnalise;

    public AnaliseIa() {
    }

    @PrePersist
    public void prePersist() {
        if (dataAnalise == null) {
            dataAnalise = LocalDateTime.now();
        }

        if (aceitaCidadao == null) {
            aceitaCidadao = false;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public void setSolicitacao(Solicitacao solicitacao) {
        this.solicitacao = solicitacao;
    }

    public Categoria getCategoriaSugerida() {
        return categoriaSugerida;
    }

    public void setCategoriaSugerida(Categoria categoriaSugerida) {
        this.categoriaSugerida = categoriaSugerida;
    }

    public Subservico getSubservicoSugerido() {
        return subservicoSugerido;
    }

    public void setSubservicoSugerido(Subservico subservicoSugerido) {
        this.subservicoSugerido = subservicoSugerido;
    }

    public NivelUrgencia getUrgenciaSugerida() {
        return urgenciaSugerida;
    }

    public void setUrgenciaSugerida(NivelUrgencia urgenciaSugerida) {
        this.urgenciaSugerida = urgenciaSugerida;
    }

    public BigDecimal getScorePrioridade() {
        return scorePrioridade;
    }

    public void setScorePrioridade(BigDecimal scorePrioridade) {
        this.scorePrioridade = scorePrioridade;
    }

    public NivelPrioridade getNivelPrioridade() {
        return nivelPrioridade;
    }

    public void setNivelPrioridade(NivelPrioridade nivelPrioridade) {
        this.nivelPrioridade = nivelPrioridade;
    }

    public BigDecimal getConfianca() {
        return confianca;
    }

    public void setConfianca(BigDecimal confianca) {
        this.confianca = confianca;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public String getNomeModelo() {
        return nomeModelo;
    }

    public void setNomeModelo(String nomeModelo) {
        this.nomeModelo = nomeModelo;
    }

    public String getVersaoModelo() {
        return versaoModelo;
    }

    public void setVersaoModelo(String versaoModelo) {
        this.versaoModelo = versaoModelo;
    }

    public Boolean getAceitaCidadao() {
        return aceitaCidadao;
    }

    public void setAceitaCidadao(Boolean aceitaCidadao) {
        this.aceitaCidadao = aceitaCidadao;
    }

    public LocalDateTime getDataAnalise() {
        return dataAnalise;
    }

    public void setDataAnalise(LocalDateTime dataAnalise) {
        this.dataAnalise = dataAnalise;
    }
}