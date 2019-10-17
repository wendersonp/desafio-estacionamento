package com.conpaytest.desafioestacionamento.repositories;

import java.util.List;

import com.conpaytest.desafioestacionamento.entities.Vaga;
import com.conpaytest.desafioestacionamento.repositories.projections.PosicaoVagas;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;



public interface VagaRepository extends PagingAndSortingRepository<Vaga, String>{
    @Query("SELECT l FROM Vaga l WHERE l.ocupada = 0")
    List<PosicaoVagas> findVagasLivres();

    @Query("SELECT COUNT(l) FROM Vaga l WHERE l.ocupada = 0")
    int countVagasLivres();
}