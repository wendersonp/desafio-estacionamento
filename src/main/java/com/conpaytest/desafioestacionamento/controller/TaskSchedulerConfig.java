package com.conpaytest.desafioestacionamento.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

//Configura o executador de tarefas agendadas
@Configuration
@EnableScheduling
public class TaskSchedulerConfig{
    public TaskScheduler taskScheduler(){
        return new ThreadPoolTaskScheduler();
    }
}