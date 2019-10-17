package com.conpaytest.desafioestacionamento.repositories.projections;

import com.conpaytest.desafioestacionamento.entities.Vaga;

public class RelatorioByVagaDTO{
    public final Vaga vaga;
    public final long countVeiculos;
    public final double lucro;

    public RelatorioByVagaDTO(Vaga vaga, long countVeiculos, double lucro) {
        this.vaga = vaga;
        this.countVeiculos = countVeiculos;
        this.lucro = lucro;
    }
}