package com.example.Sisegg.DTO;

import com.example.Sisegg.models.Produtor;

public record ProdutorResponseDTO(
    Long id,
    String nome,
    String cpf,
    String cnpj,
    String dataNascimento,
    String sexo,
    String email,
    String telefone,
    String endereco,
    Double imposto,
    Double repasse,
    String repasseSobre,
    String formaRepasse
) {
    public ProdutorResponseDTO(Produtor produtor) {
        this(produtor.getId(), produtor.getNome(), produtor.getCpf(), produtor.getCnpj(),
             produtor.getDataNascimento(), produtor.getSexo(), produtor.getEmail(),
             produtor.getTelefone(), produtor.getEndereco(), produtor.getImposto(),
             produtor.getRepasse(), produtor.getRepasseSobre(), produtor.getFormaRepasse());
    }
}
