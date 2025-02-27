package com.example.Sisegg.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parcela_pagamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ParcelaPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proposta_id", nullable = false)
    @JsonIgnore
    private PropostaApolice proposta;

    private int numeroParcela;

    @Column(nullable = false)
    private Double valorParcela;

    @Column(nullable = false)
    private String dataVencimento;

    @Column(nullable = false)
    private Boolean pago; // Se a parcela foi paga ou não

    public ParcelaPagamento(PropostaApolice proposta, int numeroParcela, Double valorParcela, String dataVencimento, Boolean pago) {
        this.proposta = proposta;
        this.numeroParcela = numeroParcela;
        this.valorParcela = valorParcela;
        this.dataVencimento = dataVencimento;
        this.pago = pago;
    }
}
