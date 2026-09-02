package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.auditoria.PaginaAuditoriaResponse;
import br.com.fiap.enterprise_challenge3.model.RegistroAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.AcaoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.ResultadoAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoAtorAuditoria;
import br.com.fiap.enterprise_challenge3.model.enums.TipoRecursoAuditoria;
import br.com.fiap.enterprise_challenge3.repository.RegistroAuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Transactional(readOnly = true)
public class ConsultaAuditoriaService {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;
    private static final int PERIODO_MAXIMO_DIAS = 366;

    private final RegistroAuditoriaRepository auditoriaRepository;

    public ConsultaAuditoriaService(
            RegistroAuditoriaRepository auditoriaRepository
    ) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public PaginaAuditoriaResponse consultar(
            TipoAtorAuditoria tipoAtor,
            Long atorId,
            AcaoAuditoria acao,
            TipoRecursoAuditoria tipoRecurso,
            String recursoId,
            ResultadoAuditoria resultado,
            LocalDate inicio,
            LocalDate fim,
            int pagina,
            int tamanho
    ) {
        validarPaginacao(
                pagina,
                tamanho
        );

        validarPeriodo(
                inicio,
                fim
        );

        String recursoIdNormalizado =
                normalizarRecursoId(recursoId);

        LocalDateTime inicioInclusivo =
                inicio == null
                        ? null
                        : inicio.atStartOfDay();

        LocalDateTime fimExclusivo =
                fim == null
                        ? null
                        : fim.plusDays(1).atStartOfDay();

        Sort ordenacao = Sort
                .by(
                        Sort.Direction.DESC,
                        "dataEvento"
                )
                .and(
                        Sort.by(
                                Sort.Direction.DESC,
                                "id"
                        )
                );

        PageRequest paginacao =
                PageRequest.of(
                        pagina,
                        tamanho,
                        ordenacao
                );

        Page<RegistroAuditoria> resultadoConsulta =
                auditoriaRepository.consultar(
                        tipoAtor,
                        atorId,
                        acao,
                        tipoRecurso,
                        recursoIdNormalizado,
                        resultado,
                        inicioInclusivo,
                        fimExclusivo,
                        paginacao
                );

        return PaginaAuditoriaResponse.fromPage(
                resultadoConsulta
        );
    }

    private void validarPaginacao(
            int pagina,
            int tamanho
    ) {
        if (pagina < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O número da página não pode ser negativo"
            );
        }

        if (
                tamanho < 1
                        || tamanho > TAMANHO_MAXIMO_PAGINA
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O tamanho da página deve estar entre 1 e 100"
            );
        }
    }

    private void validarPeriodo(
            LocalDate inicio,
            LocalDate fim
    ) {
        if (
                inicio != null
                        && fim != null
                        && inicio.isAfter(fim)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data inicial não pode ser posterior à data final"
            );
        }

        if (inicio != null && fim != null) {
            long quantidadeDias =
                    ChronoUnit.DAYS.between(
                            inicio,
                            fim
                    ) + 1;

            if (quantidadeDias > PERIODO_MAXIMO_DIAS) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A consulta aceita períodos de até 366 dias"
                );
            }
        }
    }

    private String normalizarRecursoId(
            String recursoId
    ) {
        if (
                recursoId == null
                        || recursoId.isBlank()
        ) {
            return null;
        }

        String valorNormalizado =
                recursoId.trim();

        if (valorNormalizado.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O identificador do recurso deve possuir no máximo 100 caracteres"
            );
        }

        return valorNormalizado;
    }
}