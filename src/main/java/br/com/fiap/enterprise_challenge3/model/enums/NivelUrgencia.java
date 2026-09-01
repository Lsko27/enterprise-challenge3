package br.com.fiap.enterprise_challenge3.model.enums;

public enum NivelUrgencia {

    BAIXA(1),
    MEDIA(2),
    ALTA(3),
    CRITICA(4);

    private final int prioridade;

    NivelUrgencia(int prioridade) {
        this.prioridade = prioridade;
    }

    public int getPrioridade() {
        return prioridade;
    }
}