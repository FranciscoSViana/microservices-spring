package br.com.fsv.microservice.loja.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import br.com.fsv.microservice.loja.model.Compra;

@Repository
public interface CompraRepository extends CrudRepository<Compra, Long> {

}
