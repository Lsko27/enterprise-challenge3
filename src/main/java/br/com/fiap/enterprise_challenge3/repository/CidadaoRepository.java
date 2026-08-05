package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.model.Cidadao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CidadaoRepository
        extends JpaRepository<Cidadao, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );

    Optional<Cidadao> findByCpf(String cpf);

    List<Cidadao> findAllByAtivoTrueOrderByNomeAsc();
}