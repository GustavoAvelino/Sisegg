package com.example.Sisegg.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.Sisegg.DTO.ImportacaoDTO;
import com.example.Sisegg.services.ImportacaoService;

@RestController
@RequestMapping("/api/importacao")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ImportacaoController {

    @Autowired
    private ImportacaoService importacaoService;

    @PostMapping
    public ResponseEntity<ImportacaoDTO> importarArquivo(@RequestParam("file") MultipartFile file) {
        try {
            // Processa o arquivo e extrai os dados do PDF
            ImportacaoDTO importacaoDTO = importacaoService.processarArquivo(file);
            return ResponseEntity.ok(importacaoDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(null);
        }
    }
}
