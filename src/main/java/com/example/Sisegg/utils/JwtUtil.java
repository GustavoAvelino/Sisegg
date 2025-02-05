package com.example.Sisegg.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // Gera automaticamente uma chave forte (64 bytes) para HS512
    private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public static String generateToken(String username) {
        try {
            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 dia
                    .signWith(KEY) // O algoritmo é deduzido pela chave
                    .compact();
        } catch (Exception e) {
            e.printStackTrace(); // Para depuração
            throw new RuntimeException("Erro ao gerar o token JWT", e);
        }
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
