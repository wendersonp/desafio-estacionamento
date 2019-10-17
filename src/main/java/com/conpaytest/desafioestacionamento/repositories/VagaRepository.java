package com.conpaytest.desafioestacionamento.repositories;

import java.util.List;

import com.conpaytest.desafioestacionamento.entities.Vaga;
import com.conpaytest.desafioestacionamento.repositories.projections.PosicaoVagasDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;



public interface VagaRepository extends CrudRepository<Vaga, String>{
    @Query("SELECT l FROM Vaga l WHERE l.ocupada = 0")
    List<PosicaoVagasDTO> findVagasLivres();

    @Query("SELECT COUNT(l) FROM Vaga l WHERE l.ocupada = 0")
    int countVagasLivres();

    
}