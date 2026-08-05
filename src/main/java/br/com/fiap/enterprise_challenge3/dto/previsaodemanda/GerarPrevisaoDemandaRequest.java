package br.com.fiap.enterprise_challenge3.dto.previsaodemanda;

public record GerarPrevisaoDemandaRequest(
        Integer periodoHistoricoDias,
        Integer periodoPrevisaoDias,
        Integer minimoOcorrencias,
        Boolean forcarGeracao
) {

    public boolean deveForcarGeracao() {
        return Boolean.TRUE.equals(
                forcarGeracao
        );
    }
}