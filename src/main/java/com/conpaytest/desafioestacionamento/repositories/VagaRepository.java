package com.conpaytest.desafioestacionamento.repositories;

import java.util.List;

import com.conpaytest.desafioestacionamento.entities.Vaga;
import com.conpaytest.desafioestacionamento.repositories.projections.PosicaoVagasDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;



public interface VagaRepository extends CrudRepository<Vaga, String>{

    //Obtem vagas livres da tabela de vagas
    @Query("SELECT l FROM Vaga l WHERE l.ocupada = FALSE")
    List<PosicaoVagasDTO> findVagasLivres();

    //Realiza a contagem de vagas livres
    @Query("SELECT COUNT(l) FROM Vaga l WHERE l.ocupada = FALSE")
    int countVagasLivres();

    
}