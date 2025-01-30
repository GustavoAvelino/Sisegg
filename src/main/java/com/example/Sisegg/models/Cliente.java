package com.example.Sisegg.models;

import org.springframework.stereotype.Component;

import com.example.Sisegg.DTO.ClienteRequestDTO;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "cliente")
@Entity(name = "cliente")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Component
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cnpjCpf;
    private String nome;
    private String nomeSocial;
    private String sexo;
    private String dataNascimento;
    private String estadoCivil;
    private String email;
    private String telefone;

    @ManyToOne
    @JoinColumn(name = "corretora_id") // Cria uma chave estrangeira para a tabela corretora
    private Corretora corretora;

    public Cliente(ClienteRequestDTO data) {
        this.cnpjCpf = data.cnpjCpf();
        this.nome = data.nome();
        this.nomeSocial = data.nomeSocial();
        this.sexo = data.sexo();
        this.dataNascimento = data.dataNascimento();
        this.estadoCivil = data.estadoCivil();
        this.email = data.email();
        this.telefone = data.telefone();
    }
}
