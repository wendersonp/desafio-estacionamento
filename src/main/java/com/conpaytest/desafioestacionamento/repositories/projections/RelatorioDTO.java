package com.conpaytest.desafioestacionamento.repositories.projections;

import java.util.List;

public class RelatorioDTO {
    public List<RelatorioByDataDTO> byDataDTO;
    public List<RelatorioByVagaDTO> byVagaDTO;
    public RelatorioSumDTO sumDTO;
}