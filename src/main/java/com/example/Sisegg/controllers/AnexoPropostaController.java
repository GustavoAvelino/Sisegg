package com.example.Sisegg.controllers;

import com.example.Sisegg.models.AnexoProposta;
import com.example.Sisegg.models.PropostaApolice;
import com.example.Sisegg.repositories.AnexoPropostaRepository;
import com.example.Sisegg.repositories.PropostaApoliceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/anexo-proposta")
@CrossOrigin("*")
public class AnexoPropostaController {

    // Caminho de upload compatível com Windows e Linux
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "uploads" + File.separator;

    @Autowired
    private AnexoPropostaRepository anexoPropostaRepository;

    @Autowired
    private PropostaApoliceRepository propostaApoliceRepository;

    // 🔹 Upload de arquivos para uma proposta específica
    @PostMapping("/upload/{propostaId}")
    public ResponseEntity<String> uploadAnexo(@PathVariable Long propostaId, @RequestParam("file") MultipartFile[] files) {
        Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findById(propostaId);
        if (propostaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Proposta não encontrada.");
        }

        PropostaApolice proposta = propostaOpt.get();
        StringBuilder responseMessage = new StringBuilder("Arquivos salvos: ");

        try {
            // Criar o diretório se não existir
            File pastaUpload = new File(UPLOAD_DIR);
            if (!pastaUpload.exists()) {
                boolean criado = pastaUpload.mkdirs();
                if (!criado) {
                    return ResponseEntity.internalServerError().body("Erro ao criar diretório de upload.");
                }
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue; // Ignora arquivos vazios
                }

                String filePath = UPLOAD_DIR + file.getOriginalFilename();
                File destino = new File(filePath);
                file.transferTo(destino); // Salva o arquivo no sistema de arquivos

                // Salva no banco apenas o caminho
                AnexoProposta anexo = new AnexoProposta(proposta, file.getOriginalFilename(), filePath);
                anexoPropostaRepository.save(anexo);

                responseMessage.append(file.getOriginalFilename()).append(", ");
                System.out.println("📁 Arquivo salvo em: " + filePath);
            }

            return ResponseEntity.ok(responseMessage.toString());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erro ao salvar arquivos: " + e.getMessage());
        }
    }

    // 🔹 Listar anexos de uma proposta
    @GetMapping("/{propostaId}")
    public ResponseEntity<List<AnexoProposta>> getAnexos(@PathVariable Long propostaId) {
        List<AnexoProposta> anexos = anexoPropostaRepository.findByPropostaId(propostaId);
        return ResponseEntity.ok(anexos);
    }

    // 🔹 Baixar um anexo específico
    @GetMapping("/download/{anexoId}")
    public ResponseEntity<Resource> downloadAnexo(@PathVariable Long anexoId) {
        Optional<AnexoProposta> anexoOpt = anexoPropostaRepository.findById(anexoId);
        if (anexoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AnexoProposta anexo = anexoOpt.get();
        File arquivo = new File(anexo.getCaminhoArquivo());

        if (!arquivo.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(arquivo);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + anexo.getNomeArquivo() + "\"")
                .body(resource);
    }

    // 🔹 Excluir um anexo
    @DeleteMapping("/delete/{anexoId}")
    public ResponseEntity<String> deletarAnexo(@PathVariable Long anexoId) {
        Optional<AnexoProposta> anexoOpt = anexoPropostaRepository.findById(anexoId);
        if (anexoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AnexoProposta anexo = anexoOpt.get();
        File arquivo = new File(anexo.getCaminhoArquivo());

        // Deleta o arquivo do sistema de arquivos
        if (arquivo.exists()) {
            arquivo.delete();
        }

        anexoPropostaRepository.deleteById(anexoId);
        return ResponseEntity.ok("Anexo deletado com sucesso.");
    }
}
