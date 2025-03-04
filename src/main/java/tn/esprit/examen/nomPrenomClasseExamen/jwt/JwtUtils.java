package tn.esprit.examen.nomPrenomClasseExamen.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import tn.esprit.examen.nomPrenomClasseExamen.services.SubDetailsImpl;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${bezkoder.app.jwtSecret}")
    private String jwtSecret;

    @Value("${bezkoder.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        SubDetailsImpl userPrincipal = (SubDetailsImpl) authentication.getPrincipal();
        logger.debug("🔑 Génération du JWT pour l'utilisateur : {}", userPrincipal.getEmail());

        return Jwts.builder()
                .setSubject(userPrincipal.getEmail())  // Email comme sujet
                .claim("iduser", userPrincipal.getId()) // Ajout de l'ID utilisateur
                .claim("roles", userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
                )
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256) // Encodage de la clé
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("iduser", Long.class); // Récupération de l'ID utilisateur
    }

    public boolean validateJwtToken(String authToken) {
        try {
            logger.debug("✅ Validation du JWT : {}", authToken);

            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("❌ Signature JWT invalide : {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("❌ JWT mal formé : {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("❌ JWT expiré : {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("❌ JWT non supporté : {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("❌ JWT claims string vide : {}", e.getMessage());
        }

        return false;
    }
}
