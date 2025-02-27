package com.example.Sisegg.controllers;

import com.example.Sisegg.models.AnexoMulticalculo;
import com.example.Sisegg.models.Multicalculo;
import com.example.Sisegg.repositories.AnexoMulticalculoRepository;
import com.example.Sisegg.repositories.MulticalculoRepository;
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
@RequestMapping("/anexo-multicalculo")
@CrossOrigin("*")
public class AnexoMulticalculoController {

    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "uploads" + File.separator;

    private final AnexoMulticalculoRepository anexoMulticalculoRepository;
    private final MulticalculoRepository multicalculoRepository;

    public AnexoMulticalculoController(AnexoMulticalculoRepository anexoMulticalculoRepository, MulticalculoRepository multicalculoRepository) {
        this.anexoMulticalculoRepository = anexoMulticalculoRepository;
        this.multicalculoRepository = multicalculoRepository;
    }

    @PostMapping("/upload/{multicalculoId}")
    public ResponseEntity<String> uploadAnexo(@PathVariable Long multicalculoId, @RequestParam("file") MultipartFile[] files) {
        Optional<Multicalculo> multicalculoOpt = multicalculoRepository.findById(multicalculoId);
        if (multicalculoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Multicálculo não encontrado.");
        }

        Multicalculo multicalculo = multicalculoOpt.get();
        StringBuilder responseMessage = new StringBuilder("Arquivos salvos: ");

        try {
            File pastaUpload = new File(UPLOAD_DIR);
            if (!pastaUpload.exists() && !pastaUpload.mkdirs()) {
                return ResponseEntity.internalServerError().body("Erro ao criar diretório de upload.");
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                String filePath = UPLOAD_DIR + file.getOriginalFilename();
                File destino = new File(filePath);
                file.transferTo(destino);

                AnexoMulticalculo anexo = new AnexoMulticalculo(multicalculo, file.getOriginalFilename(), filePath);
                anexoMulticalculoRepository.save(anexo);

                responseMessage.append(file.getOriginalFilename()).append(", ");
            }

            return ResponseEntity.ok(responseMessage.toString());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erro ao salvar arquivos: " + e.getMessage());
        }
    }

    @GetMapping("/{multicalculoId}")
    public ResponseEntity<List<AnexoMulticalculo>> getAnexos(@PathVariable Long multicalculoId) {
        List<AnexoMulticalculo> anexos = anexoMulticalculoRepository.findByMulticalculoId(multicalculoId);
        return ResponseEntity.ok(anexos);
    }

    @GetMapping("/download/{anexoId}")
    public ResponseEntity<Resource> downloadAnexo(@PathVariable Long anexoId) {
        Optional<AnexoMulticalculo> anexoOpt = anexoMulticalculoRepository.findById(anexoId);
        if (anexoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AnexoMulticalculo anexo = anexoOpt.get();
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

    @DeleteMapping("/delete/{anexoId}")
    public ResponseEntity<String> deletarAnexo(@PathVariable Long anexoId) {
        Optional<AnexoMulticalculo> anexoOpt = anexoMulticalculoRepository.findById(anexoId);
        if (anexoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AnexoMulticalculo anexo = anexoOpt.get();
        File arquivo = new File(anexo.getCaminhoArquivo());

        if (arquivo.exists()) {
            arquivo.delete();
        }

        anexoMulticalculoRepository.deleteById(anexoId);
        return ResponseEntity.ok("Anexo deletado com sucesso.");
    }
}
