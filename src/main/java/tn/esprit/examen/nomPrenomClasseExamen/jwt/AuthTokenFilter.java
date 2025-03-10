package tn.esprit.examen.nomPrenomClasseExamen.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        String jwt = parseJwt(request);

        if (jwt == null) {
            logger.warn("⛔ Aucun token JWT trouvé pour {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (!jwtUtils.validateJwtToken(jwt)) {
                logger.warn("⛔ Token JWT invalide !");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
                return;
            }

            String email = jwtUtils.getUserNameFromJwtToken(jwt);
            UserDetails userDetails = subDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("✅ Utilisateur authentifié : {}", email);

        } catch (ExpiredJwtException ex) {
            logger.error("⌛ Token expiré !");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
        } catch (UsernameNotFoundException ex) {
            logger.error("🔍 Utilisateur non trouvé !");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
        } catch (Exception ex) {
            logger.error("❌ Erreur d'authentification : {}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        return (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) ? headerAuth.substring(7) : null;
    }
}
