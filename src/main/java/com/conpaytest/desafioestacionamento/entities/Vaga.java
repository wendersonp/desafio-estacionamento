package com.conpaytest.desafioestacionamento.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;



@Entity
public class Vaga{
    //Identificação Unica da vaga
    @Id
    private String idVaga;

    //Determina se a vaga está ocupada
    @Column(nullable = false)
    private boolean ocupada = false;

    private int posicaoX;
    
    private int posicaoY;

    protected Vaga(){}

    public Vaga(String idVaga, int posicaoX, int posicaoY){
        this.idVaga = idVaga;
        this.posicaoX = posicaoX;
        this.posicaoY = posicaoY;
    }

    public String getIdVaga() {
        return idVaga;
    }

    public void setIdVaga(String idVaga) {
        this.idVaga = idVaga;
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
    }

    public int getPosicaoX() {
        return posicaoX;
    }

    public void setPosicaoX(int posicaoX) {
        this.posicaoX = posicaoX;
    }

    public int getPosicaoY() {
        return posicaoY;
    }

    public void setPosicaoY(int posicaoY) {
        this.posicaoY = posicaoY;
    }


    
}