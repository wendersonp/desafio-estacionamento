package com.conpaytest.desafioestacionamento;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.conpaytest.desafioestacionamento.entities.Registro;
import com.conpaytest.desafioestacionamento.entities.Vaga;
import com.conpaytest.desafioestacionamento.repositories.RegistroRepository;
import com.conpaytest.desafioestacionamento.repositories.VagaRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j

//Carrega alguns valores pre configurados no banco de dados
public class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(RegistroRepository registroRepo, VagaRepository vagaRepo){
        return args -> {
            List<Vaga> vagas = new ArrayList<>();
            //Geração das vagas de estacionamento
            List<String> letters = Arrays.asList("A", "B", "C", "D");
            for(int i = 0; i < 4; i++){
                for(int j = 1; j <= 4; j++){
                    Vaga vaga = new Vaga(letters.get(i) + Integer.toString(j), i*10, (j-1) * 10);
                    vagas.add(vaga);
                    vagaRepo.save(vaga);
                }
            }

            //Geração de 100 registros
            for(int i = 0; i < 100; i++){
                Registro reg; 
                String placa = new String();
                Random randomGenerator = new Random();

                LocalDateTime beginTime = LocalDateTime.of(2019, 9, 19, 0, 0);
                long begin = beginTime.toEpochSecond(ZoneOffset.ofHours(-3));
                
                LocalDateTime endTime = LocalDateTime.of(2019, 10, 18, 23, 59);
                long end = endTime.toEpochSecond(ZoneOffset.ofHours(-3));

                long randomEpochTime1 = ThreadLocalRandom.current().longs(begin, end).findAny().getAsLong();
                long randomEpochTime2 = ThreadLocalRandom.current().longs(begin, end).findAny().getAsLong();

                for(int j = 0; j < 3; j++){
                    placa = placa + Character.toString((char)(randomGenerator.nextInt(26) + 'A'));
                }

                placa = placa + "-";

                placa = placa + Integer.toString(randomGenerator.nextInt(10000));

                
                if(randomEpochTime1 < randomEpochTime2){
                    reg = new Registro(placa, 
                                       LocalDateTime.ofEpochSecond(randomEpochTime1, 0, ZoneOffset.ofHours(-3)),
                                       vagas.get(randomGenerator.nextInt(vagas.size())));
                    reg.setDataDeSaida(LocalDateTime.ofEpochSecond(randomEpochTime2, 0, ZoneOffset.ofHours(-3)));
                }
                else{
                     reg = new Registro(placa, 
                                       LocalDateTime.ofEpochSecond(randomEpochTime2, 0, ZoneOffset.ofHours(-3)),
                                       vagas.get(randomGenerator.nextInt(vagas.size())));
                    reg.setDataDeSaida(LocalDateTime.ofEpochSecond(randomEpochTime1, 0, ZoneOffset.ofHours(-3)));
                }
                reg.setValorEstacionamento();
                reg.setPago(true);
                registroRepo.save(reg);
                
            }
        };
    }
}