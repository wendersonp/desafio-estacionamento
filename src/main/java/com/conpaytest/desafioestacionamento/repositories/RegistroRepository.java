package com.conpaytest.desafioestacionamento.repositories;

import java.util.List;

import com.conpaytest.desafioestacionamento.entities.Registro;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface RegistroRepository extends PagingAndSortingRepository<Registro, Long>{
    @Query("SELECT r FROM Registro r JOIN r.vagaEstacionamento v WHERE v.idVaga = :id AND r.pago = 0")
    Registro findByIdVaga(@Param("id") String id);

    @Query("SELECt r FROM Registro r JOIN r.vagaEstacionamento v WHERE r.pago = 0")
    List<Registro> findPagamentosPendentes();
}