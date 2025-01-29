package com.example.Sisegg.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "veiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String placa;
    private String codigoFipe;
    private String marca;
    private String modelo;
    private Integer anoModelo;
    private Integer anoFabricacao;
    private Double valorFipe;
    private String combustivel;
    private String chassi;
    private Integer passageiros;
    private Boolean financiado;
    private Boolean chassiRemarcado;
    private Boolean kitGas;
    private Boolean plotadoOuAdesivado;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente; // Ajuste conforme sua entidade

    public Veiculo(String placa, String codigoFipe, String marca, String modelo, Integer anoModelo,
                   Integer anoFabricacao, Double valorFipe, String combustivel, String chassi,
                   Integer passageiros, Boolean financiado, Boolean chassiRemarcado,
                   Boolean kitGas, Boolean plotadoOuAdesivado, Cliente cliente) {
        this.placa = placa;
        this.codigoFipe = codigoFipe;
        this.marca = marca;
        this.modelo = modelo;
        this.anoModelo = anoModelo;
        this.anoFabricacao = anoFabricacao;
        this.valorFipe = valorFipe;
        this.combustivel = combustivel;
        this.chassi = chassi;
        this.passageiros = passageiros;
        this.financiado = financiado;
        this.chassiRemarcado = chassiRemarcado;
        this.kitGas = kitGas;
        this.plotadoOuAdesivado = plotadoOuAdesivado;
        this.cliente = cliente;
    }
}
