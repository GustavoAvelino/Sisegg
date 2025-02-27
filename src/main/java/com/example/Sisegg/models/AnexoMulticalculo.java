package com.example.Sisegg.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "anexo_multicalculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class AnexoMulticalculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "multicalculo_id", nullable = false)
    @JsonIgnore
    private Multicalculo multicalculo;

    @Column(nullable = false, length = 255)
    private String nomeArquivo;

    @Column(nullable = false, length = 500) // Armazena o caminho do arquivo
    private String caminhoArquivo;

    public AnexoMulticalculo(Multicalculo multicalculo, String nomeArquivo, String caminhoArquivo) {
        this.multicalculo = multicalculo;
        this.nomeArquivo = nomeArquivo;
        this.caminhoArquivo = caminhoArquivo;
    }
}
