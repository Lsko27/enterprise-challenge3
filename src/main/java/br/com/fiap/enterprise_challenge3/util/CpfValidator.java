package br.com.fiap.enterprise_challenge3.util;

public final class CpfValidator {

    private CpfValidator() {
    }

    public static boolean isValid(String cpfInformado) {
        if (cpfInformado == null) {
            return false;
        }

        String cpf = cpfInformado.replaceAll("\\D", "");

        if (cpf.length() != 11) {
            return false;
        }

        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int primeiroDigito = calcularDigito(cpf, 9, 10);
        int segundoDigito = calcularDigito(cpf, 10, 11);

        return primeiroDigito == Character.getNumericValue(cpf.charAt(9))
                && segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    private static int calcularDigito(
            String cpf,
            int quantidade,
            int pesoInicial
    ) {
        int soma = 0;
        int peso = pesoInicial;

        for (int i = 0; i < quantidade; i++) {
            int numero = Character.getNumericValue(cpf.charAt(i));
            soma += numero * peso;
            peso--;
        }

        int resultado = 11 - (soma % 11);

        return resultado >= 10 ? 0 : resultado;
    }
}