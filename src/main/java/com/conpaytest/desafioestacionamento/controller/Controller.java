package com.conpaytest.desafioestacionamento.controller;

import com.conpaytest.desafioestacionamento.controller.exceptions.VagaOcupadaException;
import com.conpaytest.desafioestacionamento.entities.Registro;
import com.conpaytest.desafioestacionamento.entities.Vaga;
import com.conpaytest.desafioestacionamento.repositories.RegistroRepository;
import com.conpaytest.desafioestacionamento.repositories.VagaRepository;
import com.conpaytest.desafioestacionamento.repositories.projections.PosicaoVagasDTO;
import com.conpaytest.desafioestacionamento.repositories.projections.RelatorioDTO;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller{

    @Autowired
    public VagaRepository vagaRepo;

    @Autowired
    public RegistroRepository registroRepo;
    
    @RequestMapping("")
    String test(){
        return "Hello World!";
    }

    @RequestMapping(value = "/vagas/quantidade", method = RequestMethod.GET)
    int getQuantidadeVagas(){
        return vagaRepo.countVagasLivres();
    }

    @RequestMapping(value = "/vagas", method = RequestMethod.GET)
    ResponseEntity<List<PosicaoVagasDTO>> getVagas(){
        List<PosicaoVagasDTO> vagas = vagaRepo.findVagasLivres();
        if(vagas.size() < 0){
            return ResponseEntity.notFound().build();
        }
        else{
            return ResponseEntity.accepted().body(vagas);
        }
    }
    
    @RequestMapping(value = "/estacionar/{idVaga}", method = RequestMethod.POST)
    ResponseEntity<Registro> estacionar(
        @PathVariable("idVaga") String idVaga,
        @RequestParam("placa_veiculo") String placaVeiculo,
        @RequestParam(value = "data_entrada", required = false) Integer dataEntrada,
        @RequestParam(value = "data_saida", required = false) Integer dataSaida){
        
        Optional<Vaga> vagaOptional = vagaRepo.findById(idVaga);
        //if(vagaOptional == null)
        ///  return ResponseEntity.notFound().build();
        Vaga vaga = vagaOptional.get();
        
        if(vaga.isOcupada())
            throw new VagaOcupadaException("Vaga esta ocupada");
        
        LocalDateTime LocalDataEntrada;
        LocalDateTime LocalDataSaida;

        if(dataEntrada == null)
            LocalDataEntrada = LocalDateTime.now();
        else
            LocalDataEntrada = LocalDateTime.ofEpochSecond(dataEntrada.intValue(), 0, ZoneOffset.ofHours(-3));

        Registro regEstacionamento = new Registro(placaVeiculo, LocalDataEntrada, vaga);
        System.out.println(regEstacionamento.getPlacaVeiculo());
        System.out.println(regEstacionamento.getDataDeEntrada().toString());
        vaga.setOcupada(true);

        if(dataSaida != null && dataSaida.intValue() > dataEntrada.intValue()){
            LocalDataSaida = LocalDateTime.ofEpochSecond(dataEntrada.intValue(), 0, ZoneOffset.ofHours(-3));
            regEstacionamento.setDataDeSaida(LocalDataSaida);
            regEstacionamento.setValorEstacionamento();
        }

        registroRepo.save(regEstacionamento);
        vagaRepo.save(vaga);
        
        return ResponseEntity.ok().body(regEstacionamento);
    }

    @RequestMapping(value = "/pagamento/pendentes", method = RequestMethod.GET)
    List<Registro> obterFaturasPendentes(){
        List<Registro> pendentes = registroRepo.findPagamentosPendentes();
        pendentes.forEach((value) -> {
            if(value.getDataDeSaida() == null){
                value.setDataDeSaida(LocalDateTime.now());
                value.setValorEstacionamento();
            }
        });

        return pendentes;
    }

    @RequestMapping(value = "/pagamento/{idVaga}", method = RequestMethod.PUT)
    ResponseEntity<Registro> pagar(
        @PathVariable("idVaga") String idVaga,
        @RequestParam(value = "data_saida", required = false) Integer dataSaida){
        
        Registro reg = registroRepo.findByIdVaga(idVaga);
        if(reg == null)
            return ResponseEntity.notFound().build();
        LocalDateTime dataDeSaida;
        if(dataSaida != null)
            dataDeSaida = LocalDateTime.ofEpochSecond(dataSaida.intValue(), 0, ZoneOffset.ofHours(-3));
        else
            dataDeSaida = LocalDateTime.now();
        
        if(reg.getDataDeEntrada().isBefore(dataDeSaida))
            reg.setDataDeSaida(dataDeSaida);
        else
            reg.setDataDeSaida(LocalDateTime.now());

        reg.setValorEstacionamento();
            
        
        reg.setPago(true);
        Vaga vaga = vagaRepo.findById(reg.getVagaEstacionamento().getIdVaga()).get();
        //TODO: Caso a data de saida seja no futuro, marcar um schedule para liberar estacionamento apenas no futuro
        vaga.setOcupada(false);

        registroRepo.save(reg);
        vagaRepo.save(vaga);

        return ResponseEntity.ok().body(reg);
    }

    
    @RequestMapping(value = "/relatorio", method = RequestMethod.GET)
    RelatorioDTO relatorio(){
        RelatorioDTO rel = new RelatorioDTO();
        rel.byVagaDTO = registroRepo.getRelatorioByVaga();
        rel.byDataDTO = registroRepo.getRelatorioByDate();
        rel.sumDTO = registroRepo.getRelatorioSum();
        return rel;   
    }
}