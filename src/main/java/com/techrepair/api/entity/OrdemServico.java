package com.techrepair.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordens_servico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_os", nullable = false, unique = true, length = 50)
    private String numeroOs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipamento_id", nullable = false)
    private Equipamento equipamento;

    @Column(name = "defeito_relatado", columnDefinition = "TEXT", nullable = false)
    private String defeitoRelatado;

    @Column(name = "diagnostico_tecnico", columnDefinition = "TEXT")
    private String diagnosticoTecnico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatusOrdemServico status = StatusOrdemServico.ABERTA;

    @Column(name = "valor_mao_de_obra", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal valorMaoDeObra = BigDecimal.ZERO;

    @Column(name = "valor_pecas", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal valorPecas = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    public void recalcularValorTotal() {
        BigDecimal maoDeObra = this.valorMaoDeObra != null ? this.valorMaoDeObra : BigDecimal.ZERO;
        BigDecimal pecas = this.valorPecas != null ? this.valorPecas : BigDecimal.ZERO;
        this.valorTotal = maoDeObra.add(pecas);
    }

    @PrePersist
    protected void onCreate() {
        if (this.dataAbertura == null) {
            this.dataAbertura = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = StatusOrdemServico.ABERTA;
        }
        if (this.valorMaoDeObra == null) {
            this.valorMaoDeObra = BigDecimal.ZERO;
        }
        if (this.valorPecas == null) {
            this.valorPecas = BigDecimal.ZERO;
        }
        recalcularValorTotal();
    }
}
