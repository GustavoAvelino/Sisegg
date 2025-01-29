package com.example.Sisegg.models;

import org.springframework.stereotype.Component;

import com.example.Sisegg.DTO.CorretoraRequestDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "corretora")
@Entity(name = "corretora")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Component
public class Corretora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String nomefan;
    private String estado;
    private String cidade;
    private String endereco;
    private String cnpj;
    private String email;
    private String telefone;
    private String susep;
    private Double impCorretora;

    public Corretora(CorretoraRequestDTO data) {
        this.nome = data.nome();
        this.nomefan = data.nomefan();
        this.estado = data.estado();
        this.cidade = data.cidade();
        this.endereco = data.endereco();
        this.cnpj = data.cnpj();
        this.email = data.email();
        this.telefone = data.telefone();
        this.susep = data.susep();
        this.impCorretora = data.impCorretora();
    }
}
