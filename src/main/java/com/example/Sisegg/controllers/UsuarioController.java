package com.example.Sisegg.controllers;

import com.example.Sisegg.DTO.UsuarioRequestDTO;
import com.example.Sisegg.DTO.UsuarioResponseDTO;
import com.example.Sisegg.models.Corretora;
import com.example.Sisegg.models.Usuario;
import com.example.Sisegg.repositories.CorretoraRepository;
import com.example.Sisegg.repositories.UsuarioRepository;
import com.example.Sisegg.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private CorretoraRepository corretoraRepository;

    // LOGIN (rota pública)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioRequestDTO data) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        Optional<Usuario> usuarioOptional = repository.findByEmail(data.email());
        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado");
        }

        Usuario usuario = usuarioOptional.get();
        if (!passwordEncoder.matches(data.senha(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha inválida");
        }

        // Gera token JWT
        String token = JwtUtil.generateToken(usuario.getEmail());

        // Retorna o token e o nome completo do usuário
        return ResponseEntity.ok(Map.of(
            "token", token,
            "nomeCom", usuario.getNomeCom()
        ));
    }

    // SALVAR USUÁRIO (rota pública)
    @PostMapping("/save")
    public ResponseEntity<String> saveUsuario(@RequestBody UsuarioRequestDTO data) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        Corretora corretora = null;
        if (data.corretoraId() != null) {
            Optional<Corretora> corretoraOptional = corretoraRepository.findById(data.corretoraId());
            if (corretoraOptional.isPresent()) {
                corretora = corretoraOptional.get();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Corretora com o ID fornecido não encontrada.");
            }
        }

        Usuario usuarioData = new Usuario(data);
        usuarioData.setSenha(passwordEncoder.encode(data.senha()));
        usuarioData.setCorretora(corretora);

        repository.save(usuarioData);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário criado com sucesso!");
    }

    // LISTAR TODOS OS USUÁRIOS (rota protegida)
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> getAll() {
        var usuarios = repository.findAll().stream()
                                 .map(UsuarioResponseDTO::new)
                                 .toList();
        return ResponseEntity.ok(usuarios);
    }

    // BUSCAR USUÁRIO POR ID (rota protegida)
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getById(@PathVariable Long id) {
        var usuarioOpt = repository.findById(id);
        if (usuarioOpt.isPresent()) {
            return ResponseEntity.ok(new UsuarioResponseDTO(usuarioOpt.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // ATUALIZAR USUÁRIO (rota protegida)
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateUsuario(@PathVariable Long id, @RequestBody UsuarioRequestDTO data) {
        var usuarioOpt = repository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            usuario.setNomeCom(data.nomeCom());
            usuario.setEmail(data.email());

            if (data.senha() != null && !data.senha().isBlank()) {
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                usuario.setSenha(passwordEncoder.encode(data.senha()));
            }

            if (data.corretoraId() != null) {
                Optional<Corretora> corretoraOpt = corretoraRepository.findById(data.corretoraId());
                corretoraOpt.ifPresent(usuario::setCorretora);
            }

            repository.save(usuario);
            return ResponseEntity.status(HttpStatus.OK).body("Usuário atualizado com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
    }

    // DELETAR USUÁRIO (rota protegida)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUsuario(@PathVariable Long id) {
        var usuarioOpt = repository.findById(id);
        if (usuarioOpt.isPresent()) {
            repository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Usuário deletado com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
    }

    // BUSCAR USUÁRIOS POR PARÂMETROS (rota protegida)
    @GetMapping("/search")
    public ResponseEntity<List<UsuarioResponseDTO>> searchUsuario(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String nomeCom
    ) {
        if (id != null) {
            var usuarioOpt = repository.findById(id);
            if (usuarioOpt.isPresent()) {
                return ResponseEntity.ok(List.of(new UsuarioResponseDTO(usuarioOpt.get())));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else if (nomeCom != null && !nomeCom.isBlank()) {
            var usuarios = repository.findByNomeComContainingIgnoreCase(nomeCom).stream()
                                     .map(UsuarioResponseDTO::new)
                                     .toList();
            if (usuarios.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(usuarios);
        } else {
            var usuarios = repository.findAll().stream()
                                     .map(UsuarioResponseDTO::new)
                                     .toList();
            return ResponseEntity.ok(usuarios);
        }
    }
}
