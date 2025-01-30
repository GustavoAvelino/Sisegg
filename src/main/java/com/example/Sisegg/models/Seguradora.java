package com.example.Sisegg.models;

import org.springframework.stereotype.Component;
import com.example.Sisegg.DTO.SeguradoraRequestDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "seguradora")
@Entity(name = "seguradora")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Component
public class Seguradora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String nomefan;
    private String cnpj;
    private String email;
    private String telefone;
    private String susep;
    private Double impSeguradora;

    // Nova relação com Corretora
    @ManyToOne
    @JoinColumn(name = "corretora_id") 
    private Corretora corretora;

    public Seguradora(SeguradoraRequestDTO data) {
        this.nome = data.nome();
        this.nomefan = data.nomefan();
        this.cnpj = data.cnpj();
        this.email = data.email();
        this.telefone = data.telefone();
        this.susep = data.susep();
        this.impSeguradora = data.impSeguradora();
        // Não setamos a corretora aqui pois recebemos só ID no DTO
        // A Controller cuidará de buscar e setar a entidade Corretora.
    }
}
