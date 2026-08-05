package br.com.fiap.enterprise_challenge3.service;

import br.com.fiap.enterprise_challenge3.dto.ServidorResponse;
import br.com.fiap.enterprise_challenge3.model.Servidor;
import br.com.fiap.enterprise_challenge3.repository.ServidorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ServidorService {

    private final ServidorRepository servidorRepository;

    public ServidorService(
            ServidorRepository servidorRepository
    ) {
        this.servidorRepository = servidorRepository;
    }

    public ServidorResponse buscarMeuPerfil(
            Long servidorId
    ) {
        Servidor servidor = servidorRepository
                .findById(servidorId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Servidor não encontrado"
                        )
                );

        return ServidorResponse.fromEntity(servidor);
    }
}