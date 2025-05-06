package tn.esprit.examen.nomPrenomClasseExamen.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import tn.esprit.examen.nomPrenomClasseExamen.services.SubDetailsImpl;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${bezkoder.app.jwtSecret}")
    private String jwtSecret;

    @Value("${bezkoder.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret.getBytes(StandardCharsets.UTF_8)));
    }

    public String generateJwtToken(Authentication authentication) {
        SubDetailsImpl userPrincipal = (SubDetailsImpl) authentication.getPrincipal();
        logger.debug("🔑 Génération du JWT pour : {}", userPrincipal.getEmail());

        Map<String, Object> claims = new HashMap<>();
        claims.put("idUser", userPrincipal.getId());
        claims.put("roles", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        claims.put("firstName", userPrincipal.getFirstName());
        claims.put("lastName", userPrincipal.getLastName());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userPrincipal.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getUserEmailFromJwtToken(String token) {
        return getUserNameFromJwtToken(token);
    }

    public Long getUserIdFromJwtToken(String token) {
        return parseClaims(token).get("idUser", Long.class);
    }

    public String getUserFirstNameFromJwtToken(String token) {
        try {
            return parseClaims(token).get("firstName", String.class);
        } catch (Exception e) {
            logger.error("❌ Failed to extract first name: {}", e.getMessage());
            return null;
        }
    }

    public String getUserLastNameFromJwtToken(String token) {
        try {
            return parseClaims(token).get("lastName", String.class);
        } catch (Exception e) {
            logger.error("❌ Failed to extract last name: {}", e.getMessage());
            return null;
        }
    }

    public List<String> getRolesFromJwtToken(String token) {
        return parseClaims(token).get("roles", List.class);
    }

    public boolean validateJwtToken(String token) {
        if (token == null || token.split("\\.").length != 3) {
            logger.error("❌ Format JWT invalide : {}", token);
            return false;
        }

        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            logger.error("❌ Validation du JWT échouée : {}", e.getMessage());
            return false;
        }
    }

    public Long getCurrentUserId() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs == null) throw new RuntimeException("Pas de contexte de requête actif");

            String authHeader = (String) attrs.getAttribute("Authorization", RequestAttributes.SCOPE_REQUEST);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("⛔ Token JWT manquant ou mal formé");
            }

            String token = authHeader.substring(7).trim();
            return getUserIdFromJwtToken(token);
        } catch (Exception e) {
            logger.error("❌ Impossible de récupérer l'ID utilisateur courant : {}", e.getMessage());
            throw new RuntimeException("Token non valide ou non disponible");
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
