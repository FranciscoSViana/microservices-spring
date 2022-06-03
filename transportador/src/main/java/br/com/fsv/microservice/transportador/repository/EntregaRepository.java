package br.com.fsv.microservice.transportador.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import br.com.fsv.microservice.transportador.model.Entrega;

@Repository
public interface EntregaRepository extends CrudRepository<Entrega, Long> {

}
