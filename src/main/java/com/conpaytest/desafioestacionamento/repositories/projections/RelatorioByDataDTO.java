package com.conpaytest.desafioestacionamento.repositories.projections;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class RelatorioByDataDTO{
    public final LocalDate dataSaida;
    public final long countVeiculos;
    public final double lucro;

    public RelatorioByDataDTO(Date dataSaida, long countVeiculos, double lucro) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataSaida);
        this.dataSaida = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        this.countVeiculos = countVeiculos;
        this.lucro = lucro;
    }

}