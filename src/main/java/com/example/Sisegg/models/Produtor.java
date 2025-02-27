package com.example.Sisegg.models;

import org.springframework.stereotype.Component;
import com.example.Sisegg.DTO.ProdutorRequestDTO;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "produtor")
@Entity(name = "produtor")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Component
@Getter
@Setter
public class Produtor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String cpf;
    private String cnpj;
    private String dataNascimento;
    private String sexo;
    private String email;
    private String telefone;
    private String endereco;
    private Double imposto;
    private Double repasse;
    private String repasseSobre;
    private String formaRepasse;


    @ManyToOne
    @JoinColumn(name = "corretora_id") // Cria uma chave estrangeira para a tabela corretora
    private Corretora corretora;
    
    public Produtor(ProdutorRequestDTO data) {
        this.nome = data.nome();
        this.cpf = data.cpf();
        this.cnpj = data.cnpj();
        this.dataNascimento = data.dataNascimento();
        this.sexo = data.sexo();
        this.email = data.email();
        this.telefone = data.telefone();
        this.endereco = data.endereco();
        this.imposto = data.imposto();
        this.repasse = data.repasse();
        this.repasseSobre = data.repasseSobre();
        this.formaRepasse = data.formaRepasse();
    }
}
