package com.conpaytest.desafioestacionamento.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.conpaytest.desafioestacionamento.controller.exceptions.ForbiddenException;
import com.conpaytest.desafioestacionamento.controller.exceptions.NotFoundException;
import com.conpaytest.desafioestacionamento.entities.Registro;
import com.conpaytest.desafioestacionamento.entities.Vaga;
import com.conpaytest.desafioestacionamento.repositories.RegistroRepository;
import com.conpaytest.desafioestacionamento.repositories.VagaRepository;
import com.conpaytest.desafioestacionamento.repositories.projections.PosicaoVagasDTO;
import com.conpaytest.desafioestacionamento.repositories.projections.RelatorioByDataDTO;
import com.conpaytest.desafioestacionamento.repositories.projections.RelatorioByVagaDTO;
import com.conpaytest.desafioestacionamento.repositories.projections.RelatorioSumDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
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

    @Autowired
    TaskScheduler taskScheduler; 

    private class LiberarVagaRunnable implements Runnable{
        private Vaga vaga;

        LiberarVagaRunnable(Vaga vaga){
            this.vaga = vaga;
        }

        public void run(){
            vaga.setOcupada(false);
            vagaRepo.save(vaga);
        }
    }
    
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
        if(vagas.size() == 0){
            throw new NotFoundException("Nao Ha Vagas Disponiveis");
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
            throw new ForbiddenException("Vaga esta ocupada");
        
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

    
    @RequestMapping(value = "/saida/{idVaga}", method = RequestMethod.PUT)
    Registro registrarSaida(
        @PathVariable("idVaga") String idVaga,
        @RequestParam(value = "data_saida", required = false) Integer dataSaida){
        Registro reg = registroRepo.findByIdVaga(idVaga);
        if(reg == null){
            throw new NotFoundException("O registro para a vaga especificada nao foi encontrado");
        }

        LocalDateTime dataDeSaida;
        if(dataSaida != null)
            dataDeSaida = LocalDateTime.ofEpochSecond(dataSaida.intValue(), 0, ZoneOffset.ofHours(-3));
        else
            dataDeSaida = LocalDateTime.now().plusMinutes(5);

        if(reg.getDataDeEntrada().isBefore(dataDeSaida))
            reg.setDataDeSaida(dataDeSaida);
        else
            throw new ForbiddenException("A data de saida especificada deve ser apos a data de entrada");

        reg.setValorEstacionamento();

        registroRepo.save(reg);

        return reg;
    }

    @RequestMapping(value = "/pagamento/{idVaga}", method = RequestMethod.PUT)
    Registro pagar(
        @PathVariable("idVaga") String idVaga){
        
        Registro reg = registroRepo.findByIdVaga(idVaga);
        if(reg == null)
            throw new NotFoundException("O registro especificado nao foi encontrado");

        if(reg.getDataDeSaida() == null)
            throw new ForbiddenException("O horário de saída do veículo ainda nao foi registrado");
        
        reg.setPago(true);
        Vaga vaga = vagaRepo.findById(reg.getVagaEstacionamento().getIdVaga()).get();
        if(LocalDateTime.now().isAfter(reg.getDataDeSaida())){
            vaga.setOcupada(false);
            vagaRepo.save(vaga);
        }
        else{
            taskScheduler.schedule(new LiberarVagaRunnable(vaga), reg.getDataDeSaida().atZone(ZoneId.systemDefault()).toInstant());
        }

        registroRepo.save(reg);
        

        return reg;
    }

    
    @RequestMapping(value = "/relatorio/vaga", method = RequestMethod.GET)
    List<RelatorioByVagaDTO> relatorioVaga(){
        List<RelatorioByVagaDTO> rel = registroRepo.getRelatorioByVaga();
        if(rel == null){
            throw new NotFoundException("Nao foram encontrados registros");
        }
        return rel;
    }

    @RequestMapping(value = "/relatorio/data", method = RequestMethod.GET)
    List<RelatorioByDataDTO> relatorioData(){
        List<RelatorioByDataDTO> rel = registroRepo.getRelatorioByDate();
        if(rel == null){
            throw new NotFoundException("Nao foram encontrados registros");
        }
        return rel;
    }

    @RequestMapping(value = "/relatorio", method = RequestMethod.GET)
    RelatorioSumDTO relatorio(){
        RelatorioSumDTO rel = registroRepo.getRelatorioSum();
        if(rel == null){
            throw new NotFoundException("Nao foram encontrados registros");
        }
        return rel;
    }
}