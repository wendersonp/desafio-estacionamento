package com.conpaytest.desafioestacionamento;

import java.util.Arrays;
import java.util.List;

import com.conpaytest.desafioestacionamento.entities.Vaga;
import com.conpaytest.desafioestacionamento.repositories.RegistroRepository;
import com.conpaytest.desafioestacionamento.repositories.VagaRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j

public class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(RegistroRepository registroRepo, VagaRepository vagaRepo){
        return args -> {
            List<String> letters = Arrays.asList("A", "B", "C", "D");
            for(int i = 0; i < 4; i++){
                for(int j = 1; j <= 4; j++){
                    Vaga vaga = new Vaga(letters.get(i) + Integer.toString(j), i*10, (j-1) * 10);
                    vagaRepo.save(vaga);
                }
            }
        };
    }
}