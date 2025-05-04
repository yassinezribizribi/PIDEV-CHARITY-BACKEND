package tn.esprit.examen.nomPrenomClasseExamen.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import tn.esprit.examen.nomPrenomClasseExamen.services.SubDetailsImpl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor

public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${bezkoder.app.jwtSecret}")
    private String jwtSecret;

    @Value("${bezkoder.app.jwtExpirationMs}")
    private int jwtExpirationMs;
    public JwtUtils(@Value("${bezkoder.app.jwtSecret}") String jwtSecret,
                    @Value("${bezkoder.app.jwtExpirationMs}") int jwtExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateJwtToken(Authentication authentication) {
        SubDetailsImpl userPrincipal = (SubDetailsImpl) authentication.getPrincipal();
        logger.debug("🔑 Génération du JWT pour l'utilisateur : {}", userPrincipal.getEmail());

        // Create claims with idUser
        Map<String, Object> claims = new HashMap<>();
        claims.put("idUser", userPrincipal.getId()); // ✅ Ensure idUser is included
        claims.put("roles", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        );

        String jwtToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(userPrincipal.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
        logger.debug("Generated JWT: {}", jwtToken);  // Log the token to check its structure
        return jwtToken;


    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public Long getUserIdFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("idUser", Long.class); // Extract idUser from claims
    }
    public String getUserEmailFromJwtToken(String token) {
        return getUserNameFromJwtToken(token); // Reuse subject as email
    }
    public String getUserFirstNameFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("firstName", String.class);
        } catch (Exception e) {
            logger.error("❌ Failed to extract first name from JWT: {}", e.getMessage());
            return null;
        }
    }

    public String getUserLastNameFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("lastName", String.class);
        } catch (Exception e) {
            logger.error("❌ Failed to extract last name from JWT: {}", e.getMessage());
            return null;
        }
    }
    public boolean validateJwtToken(String authToken) {
        if (authToken == null || authToken.split("\\.").length != 3) {
            logger.error("❌ Invalid JWT format: {}", authToken);
            return false;
        }

        try {
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (Exception e) {
            logger.error("❌ JWT validation failed: {}", e.getMessage());
        }

        return false;
    }

}