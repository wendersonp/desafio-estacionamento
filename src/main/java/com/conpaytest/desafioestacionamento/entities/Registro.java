package com.conpaytest.desafioestacionamento.entities;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Registro{
    @Id
    @GeneratedValue
    private long idRegistro;

    private String placaVeiculo;

    @Column(nullable = false)
    private LocalDateTime dataDeEntrada;

    private LocalDateTime dataDeSaida;

    private double valorEstacionamento;

    @ManyToOne
    private Vaga vagaEstacionamento;

    private boolean pago;

    protected Registro(){}

    public Registro(String placaVeiculo, LocalDateTime dataDeEntrada, Vaga vagaEstacionamento){
        this.placaVeiculo = placaVeiculo;
        this.dataDeEntrada = dataDeEntrada;
        this.vagaEstacionamento = vagaEstacionamento;
        this.pago = false;

    }
    public String getPlacaVeiculo() {
        return placaVeiculo;
    }

    public void setPlacaVeiculo(String placaVeiculo) {
        this.placaVeiculo = placaVeiculo;
    }

    public LocalDateTime getDataDeEntrada() {
        return dataDeEntrada;
    }

    public void setDataDeEntrada(LocalDateTime dataDeEntrada) {
        this.dataDeEntrada = dataDeEntrada;
    }

    public LocalDateTime getDataDeSaida() {
        return dataDeSaida;
    }

    public void setDataDeSaida(LocalDateTime dataDeSaida) {
        if(dataDeSaida.isAfter(this.dataDeEntrada))
            this.dataDeSaida = dataDeSaida;
    }

    public double getValorEstacionamento() {
        return valorEstacionamento;
    }

    public void setValorEstacionamento() { //Calcula o valor de estacionamento a partir do instante de entrada e de saída
        /**
         * Até 3 Horas: 7.00
         * Cada Hora Extra: 3.00
         */
        if(this.dataDeSaida != null){
            long hours = ChronoUnit.HOURS.between(this.dataDeEntrada, this.dataDeSaida);
            if(hours <= 3){
                this.valorEstacionamento = 7.0;
            }
            else{
                this.valorEstacionamento = (double) 3 * (hours - 3) + 7; 
            }
        }
    }

    public Vaga getVagaEstacionamento() {
        return vagaEstacionamento;
    }

    public void setVagaEstacionamento(Vaga vagaEstacionamento) {
        this.vagaEstacionamento = vagaEstacionamento;
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    

}