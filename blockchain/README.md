# Protótipo Blockchain do GovAtende

## 1. Visão geral

O GovAtende utiliza trilhas de auditoria para registrar ações relevantes realizadas por cidadãos, servidores, auditores e pelo próprio sistema. Entre essas ações estão alterações de status, consultas de dados pessoais, downloads de anexos, exportações de relatórios, tentativas de acesso negadas e autenticações.

Embora esses registros sejam persistidos no banco de dados Oracle, um usuário com acesso privilegiado à infraestrutura poderia, em um cenário de comprometimento, tentar alterar ou excluir registros. Por isso, foi desenvolvido um protótipo de Smart Contract para demonstrar como a Blockchain pode complementar a auditoria tradicional.

O protótipo registra evidências criptográficas das ações auditáveis, permitindo verificar posteriormente se um registro continua íntegro.

## 2. Objetivo

O objetivo do Smart Contract `GovAtendeAudit` é fornecer uma camada complementar de integridade e rastreabilidade para os registros de auditoria do GovAtende.

A Blockchain não substitui o banco de dados Oracle. O Oracle continua sendo responsável pelos dados operacionais completos, enquanto o contrato armazena somente evidências criptográficas representadas por hashes.

Essa separação evita a exposição de dados pessoais e reduz o volume de informações armazenadas na rede Blockchain.

## 3. Problema identificado

Os registros tradicionais de auditoria dependem da segurança do banco de dados e da infraestrutura responsável por armazená-los. Caso uma conta administrativa seja comprometida, registros poderiam ser alterados ou removidos.

O protótipo Blockchain reduz esse risco ao registrar uma impressão digital criptográfica do evento. Depois que essa evidência é registrada, qualquer alteração nos dados originais produz um hash diferente, permitindo detectar a inconsistência.

É importante destacar que a Blockchain não comprova que o dado original era verdadeiro no momento do registro. Ela permite comprovar que o conteúdo utilizado para gerar a evidência não foi alterado posteriormente.

## 4. Tecnologia escolhida

O protótipo utiliza:

- Solidity `^0.8.24`;
- ambiente Remix IDE;
- compilador Solidity `0.8.34`;
- Remix VM Osaka;
- função criptográfica `keccak256`;
- contratos compatíveis com a Ethereum Virtual Machine;
- controle de acesso baseado em endereços Blockchain.

A linguagem Solidity foi escolhida por permitir a implementação de contratos executados de maneira determinística em redes compatíveis com a Ethereum Virtual Machine.

O Remix IDE foi utilizado por fornecer compilação, implantação, execução e inspeção de transações sem exigir uma infraestrutura Blockchain externa.

## 5. Arquitetura proposta

O protótipo representa a seguinte arquitetura:

```mermaid
flowchart TD
    A["Ação realizada no GovAtende"] --> B["Registro de auditoria no Oracle"]
    B --> C["Serialização padronizada do evento"]
    C --> D["Geração do hash Keccak-256"]
    D --> E["Smart Contract GovAtendeAudit"]
    E --> F["Hash, solicitação, ação e registrador"]
    B --> G["Dados completos permanecem no Oracle"]
    F --> H["Verificação de integridade"]
    G --> H