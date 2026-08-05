package br.com.fiap.enterprise_challenge3.repository;

import br.com.fiap.enterprise_challenge3.model.PrevisaoDemanda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrevisaoDemandaRepository
        extends JpaRepository<PrevisaoDemanda, Long> {
}