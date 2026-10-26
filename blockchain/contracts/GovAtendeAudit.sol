// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

/**
 * @title GovAtendeAudit
 * @notice Protótipo de registro imutável de evidências de auditoria
 *         do sistema GovAtende.
 *
 * @dev O contrato não armazena dados pessoais. Apenas o hash da
 *      evidência e metadados não sensíveis são registrados.
 */
contract GovAtendeAudit {

    struct Evidencia {
        bytes32 hashEvidencia;
        uint256 solicitacaoId;
        string acao;
        uint256 dataRegistro;
        address registrador;
    }

    address public immutable administrador;

    uint256 public totalEvidencias;

    mapping(address => bool) public registradoresAutorizados;

    mapping(bytes32 => Evidencia) private evidencias;

    mapping(uint256 => bytes32[]) private hashesPorSolicitacao;

    error ApenasAdministrador();
    error RegistradorNaoAutorizado();
    error EnderecoInvalido();
    error SolicitacaoInvalida();
    error HashInvalido();
    error AcaoObrigatoria();
    error EvidenciaJaRegistrada();
    error EvidenciaNaoEncontrada();

    event RegistradorAtualizado(
        address indexed registrador,
        bool autorizado,
        address indexed responsavel
    );

    event EvidenciaRegistrada(
        bytes32 indexed hashEvidencia,
        uint256 indexed solicitacaoId,
        string acao,
        uint256 dataRegistro,
        address indexed registrador
    );

    constructor() {
        administrador = msg.sender;
        registradoresAutorizados[msg.sender] = true;

        emit RegistradorAtualizado(
            msg.sender,
            true,
            msg.sender
        );
    }

    modifier somenteAdministrador() {
        if (msg.sender != administrador) {
            revert ApenasAdministrador();
        }

        _;
    }

    modifier somenteRegistradorAutorizado() {
        if (!registradoresAutorizados[msg.sender]) {
            revert RegistradorNaoAutorizado();
        }

        _;
    }

    /**
     * @notice Autoriza ou remove a autorização de um registrador.
     */
    function definirRegistrador(
        address registrador,
        bool autorizado
    ) external somenteAdministrador {
        if (registrador == address(0)) {
            revert EnderecoInvalido();
        }

        registradoresAutorizados[registrador] = autorizado;

        emit RegistradorAtualizado(
            registrador,
            autorizado,
            msg.sender
        );
    }

    /**
     * @notice Registra uma evidência de auditoria.
     *
     * @param solicitacaoId Identificador da solicitação no GovAtende.
     * @param acao Ação auditada, como ALTERACAO_STATUS_SOLICITACAO.
     * @param hashEvidencia Hash calculado fora da blockchain.
     */
    function registrarEvidencia(
        uint256 solicitacaoId,
        string calldata acao,
        bytes32 hashEvidencia
    ) external somenteRegistradorAutorizado {
        if (solicitacaoId == 0) {
            revert SolicitacaoInvalida();
        }

        if (bytes(acao).length == 0) {
            revert AcaoObrigatoria();
        }

        if (hashEvidencia == bytes32(0)) {
            revert HashInvalido();
        }

        if (evidencias[hashEvidencia].dataRegistro != 0) {
            revert EvidenciaJaRegistrada();
        }

        Evidencia memory novaEvidencia = Evidencia({
            hashEvidencia: hashEvidencia,
            solicitacaoId: solicitacaoId,
            acao: acao,
            dataRegistro: block.timestamp,
            registrador: msg.sender
        });

        evidencias[hashEvidencia] = novaEvidencia;

        hashesPorSolicitacao[solicitacaoId].push(
            hashEvidencia
        );

        totalEvidencias++;

        emit EvidenciaRegistrada(
            hashEvidencia,
            solicitacaoId,
            acao,
            block.timestamp,
            msg.sender
        );
    }

    /**
     * @notice Verifica se determinado hash já foi registrado.
     */
    function verificarEvidencia(
        bytes32 hashEvidencia
    ) external view returns (bool) {
        return evidencias[hashEvidencia].dataRegistro != 0;
    }

    /**
     * @notice Retorna os dados de uma evidência registrada.
     */
    function consultarEvidencia(
        bytes32 hashEvidencia
    ) external view returns (Evidencia memory) {
        Evidencia memory evidencia =
                        evidencias[hashEvidencia];

        if (evidencia.dataRegistro == 0) {
            revert EvidenciaNaoEncontrada();
        }

        return evidencia;
    }

    /**
     * @notice Retorna todos os hashes associados a uma solicitação.
     */
    function listarHashesPorSolicitacao(
        uint256 solicitacaoId
    ) external view returns (bytes32[] memory) {
        return hashesPorSolicitacao[solicitacaoId];
    }

    /**
     * @notice Retorna a quantidade de evidências de uma solicitação.
     */
    function quantidadeEvidenciasDaSolicitacao(
        uint256 solicitacaoId
    ) external view returns (uint256) {
        return hashesPorSolicitacao[solicitacaoId].length;
    }

    /**
     * @notice Gera um hash para demonstração no Remix.
     *
     * @dev Em produção, esse cálculo deve ocorrer no backend Java.
     *      Não devem ser enviados dados pessoais para esta função.
     */
    function calcularHashDemonstracao(
        uint256 solicitacaoId,
        string calldata acao,
        string calldata valorAnterior,
        string calldata valorNovo,
        uint256 dataEvento
    ) external pure returns (bytes32) {
        return keccak256(
            abi.encode(
                solicitacaoId,
                acao,
                valorAnterior,
                valorNovo,
                dataEvento
            )
        );
    }
}