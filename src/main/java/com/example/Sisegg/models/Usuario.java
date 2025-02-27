package com.example.Sisegg.models;

import com.example.Sisegg.DTO.UsuarioRequestDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "usuario")
@Entity(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomeCom;
    private String email;
    private String senha;
    private String confSenha;
    private int role;

    @ManyToOne
    @JoinColumn(name = "corretora_id")
    private Corretora corretora;
    
    @OneToOne
    @JoinColumn(name = "produtor_id",  nullable = true)
    private Produtor produtor;

    public Usuario(UsuarioRequestDTO data) {
        this.nomeCom = data.nomeCom();
        this.email = data.email();
        this.senha = data.senha();
        this.confSenha = data.confSenha();
        this.role = data.role();
    }
}