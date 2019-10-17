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
        //Checagem da placa do veículo
        String[] splitPlaca = placaVeiculo.split("-");
        if(splitPlaca.length == 2){
            if(splitPlaca[0].matches("[A-Z]+"))
                if(splitPlaca[1].matches("[0-9]+"))
                    this.placaVeiculo = placaVeiculo;
        }

        this.dataDeEntrada = dataDeEntrada;
        this.vagaEstacionamento = vagaEstacionamento;
        this.pago = false;

    }
    public String getPlacaVeiculo() {
        return placaVeiculo;
    }

    public void setPlacaVeiculo(String placaVeiculo) {
        //Checagem da placa do veículo
        String[] splitPlaca = placaVeiculo.split("-");
        if(splitPlaca.length == 2){
            if(splitPlaca[0].matches("[A-Z]+"))
                if(splitPlaca[1].matches("[0-9]+"))
                    this.placaVeiculo = placaVeiculo;
        }
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

    public void setValorEstacionamento() { //Seta o valor do estacionamento a partir da data de Saida
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