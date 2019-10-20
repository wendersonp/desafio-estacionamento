package com.conpaytest.desafioestacionamento.repositories;

import java.util.List;

import com.conpaytest.desafioestacionamento.entities.Registro;
import com.conpaytest.desafioestacionamento.repositories.projections.RelatorioByDataDTO;
import com.conpaytest.desafioestacionamento.repositories.projections.RelatorioByVagaDTO;
import com.conpaytest.desafioestacionamento.repositories.projections.RelatorioSumDTO;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface RegistroRepository extends PagingAndSortingRepository<Registro, String>{

    //Encontra registro relacionado a alguma vaga que nao foi pago
    @Query("SELECT r FROM Registro r JOIN r.vagaEstacionamento v WHERE v.idVaga = :id AND r.pago = FALSE")
    Registro findByIdVaga(@Param("id") String id);

    //Encontra todos os registros que ainda nao foram pagos na tabela de registros
    @Query("SELECT r FROM Registro r JOIN r.vagaEstacionamento v WHERE r.pago = FALSE")
    List<Registro> findPagamentosPendentes();

    //Query para obter relatorio separado por vaga de estacionamento
    @Query(
        "SELECT NEW com.conpaytest.desafioestacionamento.repositories.projections.RelatorioByVagaDTO(r.vagaEstacionamento, COUNT(r.idRegistro), SUM(r.valorEstacionamento))" +
        " FROM Registro r " + 
        " JOIN r.vagaEstacionamento" +
        " WHERE r.pago = TRUE" +
        " GROUP BY r.vagaEstacionamento.idVaga" +
        " ORDER BY r.vagaEstacionamento.idVaga ASC")
    List<RelatorioByVagaDTO> getRelatorioByVaga();

    //Query para obter relatorio separado por data
    @Query(
        "SELECT NEW com.conpaytest.desafioestacionamento.repositories.projections.RelatorioByDataDTO(CAST(r.dataDeSaida AS date), COUNT(r.idRegistro), SUM(r.valorEstacionamento))" +
        " FROM Registro r" +
        " WHERE r.pago = TRUE" +
        " GROUP BY CAST(r.dataDeSaida AS date)" +
        " ORDER BY CAST(r.dataDeSaida AS date) DESC")
    List<RelatorioByDataDTO> getRelatorioByDate(Pageable page);

    //Query para obter a soma total de vagas e lucro total
    @Query(
        "SELECT NEW com.conpaytest.desafioestacionamento.repositories.projections.RelatorioSumDTO(COUNT(r.idRegistro), SUM(r.valorEstacionamento))" +
        " FROM Registro r" +
        " WHERE r.pago = TRUE")
    RelatorioSumDTO getRelatorioSum();

}