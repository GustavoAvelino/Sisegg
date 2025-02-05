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
            "corretoraId", corretoraId,
            "role", usuario.getRole()
        ));
    }

    // SALVAR USUÁRIO (rota pública ou protegida - depende de sua regra)
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
    
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
    
        Usuario usuario = usuarioOpt.get();
        usuario.setNomeCom(data.nomeCom());
        usuario.setEmail(data.email());
    
        // Se veio senha e não estiver vazia, atualiza.
        if (data.senha() != null && !data.senha().isBlank()) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            usuario.setSenha(passwordEncoder.encode(data.senha()));
        }
        // Senão, não faz nada (mantém a senha antiga).
    
        // Corretora
        if (data.corretoraId() != null) {
            Optional<Corretora> corOpt = corretoraRepository.findById(data.corretoraId());
            if (corOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Corretora não encontrada.");
            }
            usuario.setCorretora(corOpt.get());
        } else {
            // Se quiser remover a corretora, ajuste conforme necessidade 
            // usuario.setCorretora(null);
        }
    
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Usuário atualizado com sucesso!");
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
            @RequestParam(required = false) String nomeCom,
            @RequestParam(required = false) Long corretoraId
    ) {
        List<Usuario> usuarios;

        // Filtro por ID
        if (id != null) {
            var usuarioOpt = usuarioRepository.findById(id);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            usuarios = List.of(usuarioOpt.get());
        }
        // Filtro por nomeCom
        else if (nomeCom != null && !nomeCom.isBlank()) {
            usuarios = usuarioRepository.findByNomeComContainingIgnoreCase(nomeCom);
            if (usuarios.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        // Sem filtro, retorna todos
        else {
            usuarios = usuarioRepository.findAll();
        }

        // Filtra pela corretora, se veio corretoraId
        if (corretoraId != null) {
            usuarios = usuarios.stream()
                .filter(u -> u.getCorretora() != null && u.getCorretora().getId().equals(corretoraId))
                .toList();

            if (usuarios.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        // Monta a lista de DTO
        var usuariosDTO = usuarios.stream()
            .map(UsuarioResponseDTO::new)
            .toList();

        return ResponseEntity.ok(usuariosDTO);
    }
}
