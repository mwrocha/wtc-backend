package br.com.wtc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ── Gera token com sessionToken embutido ──────────────────────────
    public String generateToken(String email, String role, String sessionToken) {
        return Jwts.builder().subject(email).claim("role", role).claim("sessionToken", sessionToken).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + expiration)).signWith(getSigningKey()).compact();
    }

    // ── Mantém assinatura antiga para compatibilidade interna ─────────
    public String generateToken(String email, String role) {
        return generateToken(email, role, java.util.UUID.randomUUID().toString());
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ── Extrai sessionToken do payload ────────────────────────────────
    public String extractSessionToken(String token) {
        return extractAllClaims(token).get("sessionToken", String.class);
    }

    public boolean isTokenValid(String token, String email) {
        try {
            String extractedEmail = extractEmail(token);
            return extractedEmail.equals(email) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }
}