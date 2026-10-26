package br.com.fiap.enterprise_challenge3.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_ETP_PREVISAO_DEMANDA")
public class PrevisaoDemanda {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_previsao_demanda"
    )
    @SequenceGenerator(
            name = "seq_previsao_demanda",
            sequenceName = "SEQ_ETP_PREVISAO_DEMANDA",
            allocationSize = 1
    )
    @Column(name = "ID_PREVISAO")
    private Long id;

    @Column(name = "ID_CATEGORIA", nullable = false)
    private Long categoriaId;

    @Column(name = "ID_SUBSERVICO", nullable = false)
    private Long subservicoId;

    @Column(
            name = "NM_BAIRRO",
            nullable = false,
            length = 100
    )
    private String bairro;

    @Column(
            name = "NR_PERIODO_HISTORICO_DIAS",
            nullable = false
    )
    private Integer periodoHistoricoDias;

    @Column(
            name = "NR_PERIODO_PREVISAO_DIAS",
            nullable = false
    )
    private Integer periodoPrevisaoDias;

    @Column(
            name = "NR_OCORRENCIAS_HISTORICAS",
            nullable = false
    )
    private Integer ocorrenciasHistoricas;

    @Column(
            name = "NR_MEDIA_DIARIA",
            nullable = false,
            precision = 10,
            scale = 4
    )
    private BigDecimal mediaDiaria;

    @Column(
            name = "NR_TENDENCIA_PERCENTUAL",
            precision = 10,
            scale = 2
    )
    private BigDecimal tendenciaPercentual;

    @Column(
            name = "NR_QUANTIDADE_PREVISTA",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal quantidadePrevista;

    @Column(
            name = "DS_TENDENCIA",
            nullable = false,
            length = 20
    )
    private String tendencia;

    @Column(
            name = "DS_NIVEL_DEMANDA",
            nullable = false,
            length = 20
    )
    private String nivelDemanda;

    @Column(
            name = "NR_CONFIANCA",
            nullable = false,
            precision = 5,
            scale = 4
    )
    private BigDecimal confianca;

    @Column(
            name = "DS_JUSTIFICATIVA",
            length = 1000
    )
    private String justificativa;

    @Column(
            name = "NM_MODELO",
            nullable = false,
            length = 100
    )
    private String nomeModelo;

    @Column(
            name = "DS_VERSAO_MODELO",
            nullable = false,
            length = 50
    )
    private String versaoModelo;

    @Column(
            name = "NR_COEF_CORRELACAO",
            precision = 12,
            scale = 6
    )
    private BigDecimal coeficienteCorrelacao;

    @Column(
            name = "NR_COEF_DETERMINACAO",
            precision = 12,
            scale = 6
    )
    private BigDecimal coeficienteDeterminacao;

    @Column(
            name = "NR_COEF_ANGULAR",
            precision = 16,
            scale = 6
    )
    private BigDecimal coeficienteAngular;

    @Column(
            name = "NR_INTERCEPTO",
            precision = 16,
            scale = 6
    )
    private BigDecimal intercepto;

    @Column(
            name = "DS_EQUACAO",
            length = 150
    )
    private String equacao;

    @Column(
            name = "DS_FORCA_CORRELACAO",
            length = 20
    )
    private String forcaCorrelacao;

    @Column(
            name = "DS_TENDENCIA_REGRESSAO",
            length = 20
    )
    private String tendenciaRegressao;

    @Column(
            name = "NR_QTD_DIAS_REGRESSAO",
            precision = 4,
            scale = 0
    )
    private Integer quantidadeDiasRegressao;

    @Column(
            name = "NR_VARIACAO_ESTIMADA",
            precision = 10,
            scale = 2
    )
    private BigDecimal variacaoEstimadaPercentual;

    @Column(
            name = "DS_INTERPRETACAO_REGRESSAO",
            length = 1000
    )
    private String interpretacaoRegressao;

    @Column(
            name = "DT_PREVISAO",
            nullable = false
    )
    private LocalDateTime dataPrevisao;

    protected PrevisaoDemanda() {
    }

    public PrevisaoDemanda(
            Long categoriaId,
            Long subservicoId,
            String bairro,
            Integer periodoHistoricoDias,
            Integer periodoPrevisaoDias,
            Integer ocorrenciasHistoricas,
            BigDecimal mediaDiaria,
            BigDecimal tendenciaPercentual,
            BigDecimal quantidadePrevista,
            String tendencia,
            String nivelDemanda,
            BigDecimal confianca,
            String justificativa,
            String nomeModelo,
            String versaoModelo,
            LocalDateTime dataPrevisao
    ) {
        this.categoriaId = categoriaId;
        this.subservicoId = subservicoId;
        this.bairro = bairro;
        this.periodoHistoricoDias = periodoHistoricoDias;
        this.periodoPrevisaoDias = periodoPrevisaoDias;
        this.ocorrenciasHistoricas = ocorrenciasHistoricas;
        this.mediaDiaria = mediaDiaria;
        this.tendenciaPercentual = tendenciaPercentual;
        this.quantidadePrevista = quantidadePrevista;
        this.tendencia = tendencia;
        this.nivelDemanda = nivelDemanda;
        this.confianca = confianca;
        this.justificativa = justificativa;
        this.nomeModelo = nomeModelo;
        this.versaoModelo = versaoModelo;
        this.dataPrevisao = dataPrevisao;
    }

    public void aplicarAnaliseRegressao(
            BigDecimal coeficienteCorrelacao,
            BigDecimal coeficienteDeterminacao,
            BigDecimal coeficienteAngular,
            BigDecimal intercepto,
            String equacao,
            String forcaCorrelacao,
            String tendenciaRegressao,
            Integer quantidadeDiasRegressao,
            BigDecimal variacaoEstimadaPercentual,
            String interpretacaoRegressao
    ) {
        this.coeficienteCorrelacao =
                coeficienteCorrelacao;

        this.coeficienteDeterminacao =
                coeficienteDeterminacao;

        this.coeficienteAngular =
                coeficienteAngular;

        this.intercepto = intercepto;
        this.equacao = equacao;

        this.forcaCorrelacao =
                forcaCorrelacao;

        this.tendenciaRegressao =
                tendenciaRegressao;

        this.quantidadeDiasRegressao =
                quantidadeDiasRegressao;

        this.variacaoEstimadaPercentual =
                variacaoEstimadaPercentual;

        this.interpretacaoRegressao =
                interpretacaoRegressao;
    }

    public Long getId() {
        return id;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public Long getSubservicoId() {
        return subservicoId;
    }

    public String getBairro() {
        return bairro;
    }

    public Integer getPeriodoHistoricoDias() {
        return periodoHistoricoDias;
    }

    public Integer getPeriodoPrevisaoDias() {
        return periodoPrevisaoDias;
    }

    public Integer getOcorrenciasHistoricas() {
        return ocorrenciasHistoricas;
    }

    public BigDecimal getMediaDiaria() {
        return mediaDiaria;
    }

    public BigDecimal getTendenciaPercentual() {
        return tendenciaPercentual;
    }

    public BigDecimal getQuantidadePrevista() {
        return quantidadePrevista;
    }

    public String getTendencia() {
        return tendencia;
    }

    public String getNivelDemanda() {
        return nivelDemanda;
    }

    public BigDecimal getConfianca() {
        return confianca;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public String getNomeModelo() {
        return nomeModelo;
    }

    public String getVersaoModelo() {
        return versaoModelo;
    }

    public BigDecimal getCoeficienteCorrelacao() {
        return coeficienteCorrelacao;
    }

    public BigDecimal getCoeficienteDeterminacao() {
        return coeficienteDeterminacao;
    }

    public BigDecimal getCoeficienteAngular() {
        return coeficienteAngular;
    }

    public BigDecimal getIntercepto() {
        return intercepto;
    }

    public String getEquacao() {
        return equacao;
    }

    public String getForcaCorrelacao() {
        return forcaCorrelacao;
    }

    public String getTendenciaRegressao() {
        return tendenciaRegressao;
    }

    public Integer getQuantidadeDiasRegressao() {
        return quantidadeDiasRegressao;
    }

    public BigDecimal getVariacaoEstimadaPercentual() {
        return variacaoEstimadaPercentual;
    }

    public String getInterpretacaoRegressao() {
        return interpretacaoRegressao;
    }

    public LocalDateTime getDataPrevisao() {
        return dataPrevisao;
    }
}