package tn.esprit.examen.nomPrenomClasseExamen.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.esprit.examen.nomPrenomClasseExamen.services.SubscriberDetailsServiceImpl;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    private final JwtUtils jwtUtils;
    private final SubscriberDetailsServiceImpl subDetailsService;

    public AuthTokenFilter(JwtUtils jwtUtils, SubscriberDetailsServiceImpl subDetailsService) {
        this.jwtUtils = jwtUtils;
        this.subDetailsService = subDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        logger.info("📥 Requête interceptée : " + requestURI);

        // ✅ Autoriser les routes d'authentification sans filtre
        if (requestURI.startsWith("/api/auth/")) {
            logger.info("🔓 Accès autorisé sans authentification à : " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // ✅ Extraction du JWT
            String jwt = parseJwt(request);
            if (jwt == null) {
                logger.warn("⚠️ Aucun token trouvé dans la requête !");
            } else {
                logger.info("🔍 Token extrait : " + jwt);
            }

            // ✅ Validation et authentification
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Changer ici pour extraire l'email au lieu du username
                String email = jwtUtils.getUserNameFromJwtToken(jwt); // Assurez-vous que cette méthode extrait bien l'email
                logger.info("✅ Token valide pour l'utilisateur avec email : " + email);

                // Charger les détails de l'utilisateur en utilisant l'email
                UserDetails userDetails = subDetailsService.loadUserByUsername(email); // Utilisation de l'email au lieu du prénom
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.warn("⛔ Token invalide !");
            }

            // ✅ Continuer le filtre seulement si aucune erreur
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
            logger.error("❌ Le token JWT a expiré : {}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Le token a expiré.");

        } catch (MalformedJwtException ex) {
            logger.error("❌ Token JWT mal formé : {}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Token invalide.");

        } catch (Exception e) {
            logger.error("❌ Erreur d'authentification : {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Erreur d'authentification.");
        }
    }


    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
