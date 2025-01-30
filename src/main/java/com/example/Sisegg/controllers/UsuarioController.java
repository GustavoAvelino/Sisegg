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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CorretoraRepository corretoraRepository;

    // LOGIN (rota pública)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsuarioRequestDTO data) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 1) Busca o usuário pelo e-mail
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(data.email());
        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado");
        }

        // 2) Verifica a senha
        Usuario usuario = usuarioOptional.get();
        if (!passwordEncoder.matches(data.senha(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha inválida");
        }

        // 3) Gera token JWT
        String token = JwtUtil.generateToken(usuario.getEmail());

        // 4) Captura o ID da corretora (ou null, se não tiver corretora)
        Long corretoraId = (usuario.getCorretora() != null)
                ? usuario.getCorretora().getId()
                : null;

        // 5) Retorna token, nome e corretoraId
        return ResponseEntity.ok(Map.of(
            "token", token,
            "nomeCom", usuario.getNomeCom(),
            "corretoraId", corretoraId
        ));
    }

    // SALVAR USUÁRIO (rota pública)
    @PostMapping("/save")
    public ResponseEntity<String> saveUsuario(@RequestBody UsuarioRequestDTO data) {
        // 1) Criptografa a senha
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 2) Se foi informado corretoraId, busca a corretora
        Corretora corretora = null;
        if (data.corretoraId() != null) {
            Optional<Corretora> corretoraOptional = corretoraRepository.findById(data.corretoraId());
            if (corretoraOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Corretora com o ID fornecido não encontrada.");
            }
            corretora = corretoraOptional.get();
        }

        // 3) Cria o objeto Usuario a partir do DTO
        Usuario novoUsuario = new Usuario(data);
        novoUsuario.setSenha(passwordEncoder.encode(data.senha()));
        novoUsuario.setCorretora(corretora);

        // 4) Salva no banco
        usuarioRepository.save(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário criado com sucesso!");
    }

    // LISTAR TODOS OS USUÁRIOS (rota protegida)
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> getAll() {
        var usuarios = usuarioRepository.findAll().stream()
            .map(UsuarioResponseDTO::new)
            .toList();
        return ResponseEntity.ok(usuarios);
    }

    // BUSCAR USUÁRIO POR ID (rota protegida)
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getById(@PathVariable Long id) {
        var usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            return ResponseEntity.ok(new UsuarioResponseDTO(usuarioOpt.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // ATUALIZAR USUÁRIO (rota protegida)
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateUsuario(@PathVariable Long id, @RequestBody UsuarioRequestDTO data) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // 1) Atualiza campos básicos
            usuario.setNomeCom(data.nomeCom());
            usuario.setEmail(data.email());

            // 2) Se veio senha, criptografa e atualiza
            if (data.senha() != null && !data.senha().isBlank()) {
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                usuario.setSenha(passwordEncoder.encode(data.senha()));
            }

            // 3) Se veio corretoraId, vincula a corretora
            if (data.corretoraId() != null) {
                Optional<Corretora> corretoraOpt = corretoraRepository.findById(data.corretoraId());
                if (corretoraOpt.isPresent()) {
                    usuario.setCorretora(corretoraOpt.get());
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Corretora com o ID fornecido não encontrada.");
                }
            }

            // 4) Salva alterações
            usuarioRepository.save(usuario);
            return ResponseEntity.status(HttpStatus.OK).body("Usuário atualizado com sucesso!");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
    }

    // DELETAR USUÁRIO (rota protegida)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUsuario(@PathVariable Long id) {
        var usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            usuarioRepository.deleteById(id);
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
        // Filtro por ID
        if (id != null) {
            var usuarioOpt = usuarioRepository.findById(id);
            if (usuarioOpt.isPresent()) {
                return ResponseEntity.ok(List.of(new UsuarioResponseDTO(usuarioOpt.get())));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        // Filtro por nomeCom (contendo, ignorando maiúsculas e minúsculas)
        else if (nomeCom != null && !nomeCom.isBlank()) {
            var usuarios = usuarioRepository.findByNomeComContainingIgnoreCase(nomeCom).stream()
                .map(UsuarioResponseDTO::new)
                .toList();
            if (usuarios.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(usuarios);
        }
        // Sem filtro, retorna todos
        else {
            var usuarios = usuarioRepository.findAll().stream()
                .map(UsuarioResponseDTO::new)
                .toList();
            return ResponseEntity.ok(usuarios);
        }
    }
}
