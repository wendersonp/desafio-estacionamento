package com.conpaytest.desafioestacionamento.repositories.projections;

public class RelatorioSumDTO{
    public final long countVeiculos;
    public final double lucro;

    public RelatorioSumDTO(long countVeiculos, double lucro) {
        this.countVeiculos = countVeiculos;
        this.lucro = lucro;
    }
}