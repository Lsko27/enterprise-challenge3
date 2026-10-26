# Testes do Smart Contract GovAtendeAudit

## 1. Objetivo

Este documento apresenta os testes realizados no Smart Contract `GovAtendeAudit`, desenvolvido para registrar evidências criptográficas das ações auditáveis do GovAtende.

Os testes validam:

- implantação do contrato;
- geração de hashes;
- registro de evidências;
- consulta de evidências;
- rejeição de registros duplicados;
- controle de acesso;
- autorização de novos registradores;
- registro por diferentes contas autorizadas.

## 2. Ambiente de teste

| Item | Configuração |
|---|---|
| Ferramenta | Remix IDE |
| Linguagem | Solidity |
| Versão declarada | `^0.8.24` |
| Compilador utilizado | `0.8.34` |
| Ambiente | Remix VM Osaka |
| Contrato | `GovAtendeAudit` |
| Tipo de rede | Blockchain local simulada |
| Data dos testes | 26/10/2026 |

Os testes foram realizados em uma rede simulada fornecida pelo Remix. Portanto, os resultados demonstram o funcionamento lógico do contrato, mas não representam uma implantação em produção.

## 3. Contas utilizadas

### Conta administradora

```text
0x5B38Da6a701c568545dCfcB03FcB875f56beddC4
```

Essa conta realizou a implantação do contrato. Durante a implantação, ela foi definida como administradora e registradora autorizada.

### Segundo registrador

```text
0xAb8483F64d9C6d1EcF9b849Ae677dD3315835Cb2
```

Essa conta foi utilizada para testar:

- tentativa de registro sem autorização;
- autorização concedida pelo administrador;
- registro após a autorização.

## 4. Primeira evidência

A primeira evidência representa uma alteração de status registrada na solicitação `210`.

| Campo | Valor |
|---|---|
| Solicitação | `210` |
| Ação | `ALTERACAO_STATUS_SOLICITACAO` |
| Valor anterior | `EM_ANDAMENTO` |
| Valor novo | `AGUARDANDO_INFORMACOES` |
| Data do evento | `1793037998` |

Hash calculado:

```text
0x8df6f6c9fc4c87296a42678fa4e3ed49c8e74fa8e4989ff248bab208454c8390
```

O hash foi gerado por meio da função demonstrativa:

```solidity
calcularHashDemonstracao(
    210,
    "ALTERACAO_STATUS_SOLICITACAO",
    "EM_ANDAMENTO",
    "AGUARDANDO_INFORMACOES",
    1793037998
)
```

## 5. Segunda evidência

A segunda evidência representa uma alteração de status registrada na solicitação `211`.

| Campo | Valor |
|---|---|
| Solicitação | `211` |
| Ação | `ALTERACAO_STATUS_SOLICITACAO` |
| Valor anterior | `REGISTRADA` |
| Valor novo | `EM_TRIAGEM` |
| Data do evento | `1793038100` |

Hash calculado:

```text
0xe7a6080c4dcbbd40e8aae631085ec0a13ed80d685a38efc8dea661945775deb2
```

O hash foi gerado por meio da função demonstrativa:

```solidity
calcularHashDemonstracao(
    211,
    "ALTERACAO_STATUS_SOLICITACAO",
    "REGISTRADA",
    "EM_TRIAGEM",
    1793038100
)
```

## 6. Casos de teste

| Código | Teste | Resultado esperado | Resultado obtido | Situação |
|---|---|---|---|---|
| T01 | Implantar o contrato com a Conta 1 | Contrato implantado e conta autorizada | Contrato implantado corretamente | Aprovado |
| T02 | Calcular o hash da solicitação 210 | Retornar um valor `bytes32` | Primeiro hash retornado | Aprovado |
| T03 | Registrar a primeira evidência | Concluir a transação | Evidência registrada | Aprovado |
| T04 | Consultar `totalEvidencias` | Retornar `1` | Retornou `1` | Aprovado |
| T05 | Verificar o primeiro hash | Retornar `true` | Retornou `true` | Aprovado |
| T06 | Registrar novamente o primeiro hash | Rejeitar a duplicidade | Erro `EvidenciaJaRegistrada` | Aprovado |
| T07 | Consultar autorização da Conta 2 | Retornar `false` | Retornou `false` | Aprovado |
| T08 | Registrar com a Conta 2 não autorizada | Rejeitar a transação | Erro `RegistradorNaoAutorizado` | Aprovado |
| T09 | Autorizar a Conta 2 | Concluir a transação | Conta autorizada | Aprovado |
| T10 | Consultar novamente a Conta 2 | Retornar `true` | Retornou `true` | Aprovado |
| T11 | Calcular o hash da solicitação 211 | Retornar um novo `bytes32` | Segundo hash retornado | Aprovado |
| T12 | Registrar pela Conta 2 autorizada | Concluir a transação | Evidência registrada | Aprovado |
| T13 | Consultar `totalEvidencias` | Retornar `2` | Retornou `2` | Aprovado |

## 7. Teste de implantação

O contrato foi implantado pela Conta 1 na Remix VM Osaka.

Após a implantação:

- a Conta 1 foi definida como administradora;
- a Conta 1 foi autorizada como registradora;
- o contador `totalEvidencias` iniciou em zero.

O administrador é responsável por autorizar ou remover outros registradores.

## 8. Teste de registro válido

A primeira evidência foi registrada pela conta administradora utilizando:

```solidity
registrarEvidencia(
    0x8df6f6c9fc4c87296a42678fa4e3ed49c8e74fa8e4989ff248bab208454c8390,
    210,
    "ALTERACAO_STATUS_SOLICITACAO"
)
```

A transação foi concluída com sucesso e o contrato emitiu o evento `EvidenciaRegistrada`.

Após o registro:

```text
totalEvidencias = 1
```

## 9. Teste de verificação

O primeiro hash foi informado na função:

```solidity
verificarEvidencia(
    0x8df6f6c9fc4c87296a42678fa4e3ed49c8e74fa8e4989ff248bab208454c8390
)
```

Resultado:

```text
true
```

O retorno confirma que a evidência foi registrada no contrato.

## 10. Teste de duplicidade

Foi realizada uma segunda tentativa de registro utilizando o primeiro hash.

O contrato rejeitou a transação com o erro:

```text
EvidenciaJaRegistrada
```

Esse comportamento impede que uma mesma evidência seja registrada e contabilizada mais de uma vez.

## 11. Teste de acesso não autorizado

Antes de receber autorização, a Conta 2 tentou registrar a segunda evidência.

O contrato rejeitou a transação com o erro:

```text
RegistradorNaoAutorizado
```

O resultado confirma que apenas endereços autorizados podem executar a função `registrarEvidencia`.

## 12. Teste de autorização

A Conta 1, responsável pela administração do contrato, executou:

```solidity
definirRegistrador(
    0xAb8483F64d9C6d1EcF9b849Ae677dD3315835Cb2,
    true
)
```

A transação foi concluída e o contrato emitiu o evento `RegistradorAtualizado`.

Depois da autorização, a consulta:

```solidity
registradoresAutorizados(
    0xAb8483F64d9C6d1EcF9b849Ae677dD3315835Cb2
)
```

retornou:

```text
true
```

## 13. Teste de registro pelo segundo endereço

Após receber autorização, a Conta 2 registrou a segunda evidência utilizando:

```solidity
registrarEvidencia(
    0xe7a6080c4dcbbd40e8aae631085ec0a13ed80d685a38efc8dea661945775deb2,
    211,
    "ALTERACAO_STATUS_SOLICITACAO"
)
```

A transação foi concluída com sucesso.

O teste comprova que um endereço diferente do administrador pode registrar evidências, desde que tenha sido previamente autorizado.

## 14. Resultado final

Ao término dos testes, a consulta da variável:

```solidity
totalEvidencias
```

retornou:

```text
2
```

O resultado corresponde às duas evidências válidas registradas:

1. solicitação `210`, registrada pelo administrador;
2. solicitação `211`, registrada pela segunda conta autorizada.

As tentativas rejeitadas não alteraram o contador de evidências.

## 15. Evidências visuais

As capturas dos testes devem ser armazenadas em:

```text
blockchain/docs/images/
```

Nomes recomendados:

| Arquivo | Conteúdo |
|---|---|
| `01-contrato-implantado.png` | Contrato implantado no Remix |
| `02-primeiro-hash.png` | Hash da solicitação 210 |
| `03-primeiro-registro.png` | Registro da primeira evidência |
| `04-verificacao-true.png` | Verificação da primeira evidência |
| `05-duplicidade-rejeitada.png` | Erro `EvidenciaJaRegistrada` |
| `06-conta-nao-autorizada.png` | Conta 2 com autorização igual a `false` |
| `07-registro-negado.png` | Erro `RegistradorNaoAutorizado` |
| `08-autorizacao-conta-2.png` | Transação de autorização |
| `09-autorizacao-confirmada.png` | Consulta retornando `true` |
| `10-segundo-registro.png` | Registro realizado pela Conta 2 |
| `11-total-evidencias.png` | Resultado final igual a `2` |

Para exibir uma imagem neste documento, pode ser utilizada a seguinte sintaxe:

```md
![Descrição da evidência](../docs/images/nome-do-arquivo.png)
```

## 16. Conclusão

Todos os casos de teste planejados foram aprovados.

O Smart Contract demonstrou:

- geração de evidências criptográficas;
- registro de hashes;
- verificação da existência de evidências;
- rejeição de registros duplicados;
- controle de acesso administrativo;
- bloqueio de endereços não autorizados;
- autorização de novos registradores;
- registro por múltiplos endereços autorizados;
- contabilização correta das evidências.

Os testes foram executados em uma Blockchain simulada. Dessa forma, comprovam o funcionamento do protótipo, mas não representam uma implantação em uma rede Blockchain real nem uma integração automática com o backend Java.
