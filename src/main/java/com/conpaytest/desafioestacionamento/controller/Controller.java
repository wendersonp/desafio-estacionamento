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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller{

    //Funcao para validar placa inserida, testando a formatação
    boolean ValidatePlaca(String placa){
        if(placa.matches("^[A-Z]{3}-[0-9]{4}$"))
            return true;
        return false;
    }
    @Autowired
    public VagaRepository vagaRepo;

    @Autowired
    public RegistroRepository registroRepo;

    @Autowired
    TaskScheduler taskScheduler; 

    //Execução do método para liberar vaga em instante agendado
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
    
    //Puramente para teste
    @RequestMapping("")
    String test(){
        return "Bem vindo(a)!";
    }

    //Retorna quantidade de vagas de estacionamento livres
    @RequestMapping(value = "/vagas/quantidade", method = RequestMethod.GET)
    int getQuantidadeVagas(){
        return vagaRepo.countVagasLivres();
    }

    //Retorna a lista de vagas livres
    @RequestMapping(value = "/vagas", method = RequestMethod.GET)
    ResponseEntity<List<PosicaoVagasDTO>> getVagas(){
        List<PosicaoVagasDTO> vagas = vagaRepo.findVagasLivres();
        //Se não encontrar vagas livres
        if(vagas.size() == 0){
            throw new NotFoundException("Nao Ha Vagas Disponiveis");
        }
        else{
            return ResponseEntity.accepted().body(vagas);
        }
    }
    
    //Realiza estacionamento
    @RequestMapping(value = "/estacionar/{idVaga}", method = RequestMethod.POST)
    ResponseEntity<Registro> estacionar(
        @PathVariable("idVaga") String idVaga,
        @RequestParam("placa_veiculo") String placaVeiculo,
        @RequestParam(value = "data_entrada", required = false) Integer dataEntrada,
        @RequestParam(value = "data_saida", required = false) Integer dataSaida){
        
        //Encontra vaga livre
        Optional<Vaga> vagaOptional = vagaRepo.findById(idVaga);
        //Verifica se vaga foi encontrada
        if(vagaOptional == null)
            throw new NotFoundException("Vaga inserida não foi encontrada");
        Vaga vaga = vagaOptional.get();
        
        if(vaga.isOcupada())
            throw new ForbiddenException("Vaga esta ocupada");
        
        LocalDateTime LocalDataEntrada;
        LocalDateTime LocalDataSaida;
        
        //Tratamento do parametro de instante de entrada
        if(dataEntrada == null)
            LocalDataEntrada = LocalDateTime.now();
        else
            LocalDataEntrada = LocalDateTime.ofEpochSecond(dataEntrada.intValue(), 0, ZoneOffset.ofHours(0));

        //Verifica se a placa do veiculo inserida é valida
        if(!ValidatePlaca(placaVeiculo))
            throw new ForbiddenException("A identificacao de placa do veiculo inserida e invalida");
        
        //Cria um novo registro de estacionamento
        Registro regEstacionamento = new Registro(placaVeiculo, LocalDataEntrada, vaga);
        System.out.println(regEstacionamento.getPlacaVeiculo());
        System.out.println(regEstacionamento.getDataDeEntrada().toString());
        vaga.setOcupada(true);

        //Tratamento do parametro de instante de saída, se este nao existir ou for invalido (acontece antes da entrada), nao faz nada
        if(dataSaida != null && dataSaida.intValue() > dataEntrada.intValue()){
            LocalDataSaida = LocalDateTime.ofEpochSecond(dataEntrada.intValue(), 0, ZoneOffset.ofHours(0));
            regEstacionamento.setDataDeSaida(LocalDataSaida);
            regEstacionamento.setValorEstacionamento();
        }

        //Persiste as mudanças no banco de dados
        registroRepo.save(regEstacionamento);
        vagaRepo.save(vaga);
        
        return ResponseEntity.ok().body(regEstacionamento);
    }

    @RequestMapping(value = "/pagamento/pendentes", method = RequestMethod.GET)
    List<Registro> obterFaturasPendentes(){
        //Obtem os registros pendentes de pagamento
        List<Registro> pendentes = registroRepo.findPagamentosPendentes();

        //Se a data de saída nao foi realizada, utiliza o instante atual para prever o valor a ser pago
        pendentes.forEach((value) -> {
            if(value.getDataDeSaida() == null){
                value.setDataDeSaida(LocalDateTime.now());
                value.setValorEstacionamento();
            }
        });

        return pendentes;
    }


    //Registra o instante de saída de um veículo
    @RequestMapping(value = "/saida/{idVaga}", method = RequestMethod.PUT)
    Registro registrarSaida(
        @PathVariable("idVaga") String idVaga,
        @RequestParam(value = "data_saida", required = false) Integer dataSaida){
        
        //Procura o registro para realizar a mudança
        Registro reg = registroRepo.findByIdVaga(idVaga);
        if(reg == null){
            throw new NotFoundException("O registro para a vaga especificada nao foi encontrado");
        }

        //Verifica se o parametro opcional foi inserido, se não foi inserido, o instante de saída é considerado como o atual
        //Acrescido de 5 minutos
        LocalDateTime dataDeSaida;
        if(dataSaida != null)
            dataDeSaida = LocalDateTime.ofEpochSecond(dataSaida.intValue(), 0, ZoneOffset.ofHours(0));
        else
            dataDeSaida = LocalDateTime.now().plusMinutes(5);

        //Verifica se a data de saida inserida acontece antes da data de entrada, se sim, retorna excecao    
        if(reg.getDataDeEntrada().isBefore(dataDeSaida))
            reg.setDataDeSaida(dataDeSaida);
        else
            throw new ForbiddenException("A data de saida especificada deve ser apos a data de entrada");

        //Calcula o valor do estacionamento e salva as mudanças
        reg.setValorEstacionamento();
        registroRepo.save(reg);

        return reg;
    }

    //Consolida o pagamento do estacionamento no sistema
    @RequestMapping(value = "/pagamento/{idVaga}", method = RequestMethod.PUT)
    Registro pagar(
        @PathVariable("idVaga") String idVaga){
        
        //Encontra o registro a salvar as mudanças
        Registro reg = registroRepo.findByIdVaga(idVaga);
        if(reg == null)
            throw new NotFoundException("O registro especificado nao foi encontrado");

        //Verifica se o instante de saida ja foi registrado e o valor do estacionamento calculado
        if(reg.getDataDeSaida() == null)
            throw new ForbiddenException("O horário de saída do veículo ainda nao foi registrado");
        
        //Consolida o pagamento
        reg.setPago(true);

        //Encontra a vaga a ser desocupada
        Vaga vaga = vagaRepo.findById(reg.getVagaEstacionamento().getIdVaga()).get();

        //Libera a vaga do estacionamento se o instante de saida foi ultrapassado, se nao, agenda a liberação
        if(LocalDateTime.now().isAfter(reg.getDataDeSaida())){
            vaga.setOcupada(false);
            vagaRepo.save(vaga);
        }
        else{
            taskScheduler.schedule(new LiberarVagaRunnable(vaga), reg.getDataDeSaida().atZone(ZoneId.systemDefault()).toInstant());
        }

        //Consolida a mudança
        registroRepo.save(reg);
        

        return reg;
    }

    //Obter relatorio por vaga
    @RequestMapping(value = "/relatorio/vaga", method = RequestMethod.GET)
    List<RelatorioByVagaDTO> relatorioVaga(){
        List<RelatorioByVagaDTO> rel = registroRepo.getRelatorioByVaga();
        if(rel == null){
            throw new NotFoundException("Nao foram encontrados registros");
        }
        return rel;
    }

    //Obter relatorio por data
    @RequestMapping(value = "/relatorio/data", method = RequestMethod.GET)
    List<RelatorioByDataDTO> relatorioData(
        @RequestParam(value = "pagina", required = false) Integer pagina
    ){
        Pageable page = pagina != null ? PageRequest.of(pagina, 10): PageRequest.of(0, 10); 
          
        List<RelatorioByDataDTO> rel = registroRepo.getRelatorioByDate(page);
        if(rel == null){
            throw new NotFoundException("Nao foram encontrados registros");
        }
        return rel;
    }

    //Obter somas gerais
    @RequestMapping(value = "/relatorio", method = RequestMethod.GET)
    RelatorioSumDTO relatorio(){
        RelatorioSumDTO rel = registroRepo.getRelatorioSum();
        if(rel == null){
            throw new NotFoundException("Nao foram encontrados registros");
        }
        return rel;
    }
}