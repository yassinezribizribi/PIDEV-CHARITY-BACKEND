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
        logger.debug("🔑 Génération du JWT pour l'utilisateur : {}", userPrincipal.getEmail());

        Map<String, Object> claims = new HashMap<>();
        claims.put("idUser", userPrincipal.getId());
        claims.put("roles", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userPrincipal.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("idUser", Long.class);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            logger.error("❌ JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ✅ Cette méthode te permet de récupérer l'idUser sans passer manuellement le token
    public Long getCurrentUserId() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) throw new RuntimeException("Aucune requête HTTP active");

            String authHeader = (String) requestAttributes.getAttribute("Authorization", RequestAttributes.SCOPE_REQUEST);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("⛔ Token JWT manquant ou mal formaté");
            }
            String token = authHeader.substring(7).trim();
            return getUserIdFromJwtToken(token);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'utilisateur courant : {}", e.getMessage());
            throw new RuntimeException("Impossible d'extraire l'utilisateur courant depuis le token");
        }
    }
}
