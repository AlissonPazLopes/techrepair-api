package com.techrepair.api.entity;

public enum StatusOrdemServico {
    ABERTA,
    EM_ANALISE,
    AGUARDANDO_APROVACAO,
    EM_CONSERTO,
    FINALIZADA,
    CANCELADA;

    public boolean podeTransitarPara(StatusOrdemServico novoStatus) {
        if (novoStatus == null) {
            return false;
        }
        return switch (this) {
            case ABERTA -> novoStatus == EM_ANALISE || novoStatus == CANCELADA;
            case EM_ANALISE -> novoStatus == AGUARDANDO_APROVACAO || novoStatus == CANCELADA;
            case AGUARDANDO_APROVACAO -> novoStatus == EM_CONSERTO || novoStatus == CANCELADA;
            case EM_CONSERTO -> novoStatus == FINALIZADA;
            case FINALIZADA, CANCELADA -> false;
        };
    }

    public boolean isEstadoFinal() {
        return this == FINALIZADA || this == CANCELADA;
    }
}
