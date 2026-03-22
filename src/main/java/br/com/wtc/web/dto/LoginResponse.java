package br.com.wtc.web.dto;

public record LoginResponse(
        String token,
        String id,      // MongoDB ObjectId do usuário
        String email,
        String name,
        String role     // "OPERATOR" ou "CLIENT"
) {}