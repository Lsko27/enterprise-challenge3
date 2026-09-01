package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.model.Servidor;
import br.com.fiap.enterprise_challenge3.model.enums.PerfilServidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServidorRepository
        extends JpaRepository<Servidor, Long> {

    Optional<Servidor> findByMatriculaIgnoreCase(
            String matricula
    );

    boolean existsByMatriculaIgnoreCase(
            String matricula
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    boolean existsByPerfilAndAtivoTrue(
            PerfilServidor perfil
    );
}