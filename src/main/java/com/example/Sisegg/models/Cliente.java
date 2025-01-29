package com.example.Sisegg.models;

import org.springframework.stereotype.Component;

import com.example.Sisegg.DTO.ClienteRequestDTO;

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

    public Cliente(String cnpjCpf, String nome, String nomeSocial, String sexo, String dataNascimento, String estadoCivil, String email, String telefone) {

        this.cnpjCpf = cnpjCpf;

        this.nome = nome;

        this.nomeSocial = nomeSocial;

        this.sexo = sexo;

        this.dataNascimento = dataNascimento;

        this.estadoCivil = estadoCivil;

        this.email = email;

        this.telefone = telefone;

    }
   
}
