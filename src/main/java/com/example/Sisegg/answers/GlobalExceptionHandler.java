package com.example.Sisegg.answers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

// Classe que "escuta" exceções em toda a aplicação
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Lida com violações de integridade de dados (FK, constraints únicas, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        // Log, se desejado
        ex.printStackTrace();
        String mensagem = "Operação não permitida: existem registros dependentes ou violações de integridade.";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(mensagem);
    }

    /**
     * Lida com parâmetros obrigatórios ausentes.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        String mensagem = String.format("Parâmetro obrigatório '%s' está ausente.", paramName);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensagem);
    }

    /**
     * Lida com JSON malformado ou corpo de requisição inválido.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ex.printStackTrace();
        String mensagem = "O corpo da requisição está inválido ou corrompido. Verifique o formato dos dados.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensagem);
    }

    /**
     * Lida com métodos não suportados (por exemplo, chamar DELETE em endpoint que aceita só GET).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String mensagem = "Método HTTP não suportado para essa URL.";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(mensagem);
    }

    /**
     * Lida com validações de campos (caso esteja usando @Valid / Bean Validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder("Erro(s) de validação:");
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            sb.append(" [").append(error.getField()).append(": ").append(error.getDefaultMessage()).append("]");
        });
        String mensagem = sb.toString();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensagem);
    }

    /**
     * Captura qualquer outra exceção não tratada acima.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex, WebRequest request) {
        ex.printStackTrace();
        String mensagem = "Ocorreu um erro inesperado. Tente novamente ou contate o suporte.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mensagem);
    }
}
