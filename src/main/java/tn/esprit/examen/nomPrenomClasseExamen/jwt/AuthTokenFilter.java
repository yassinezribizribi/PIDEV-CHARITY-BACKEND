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
        String method = request.getMethod();
        logger.info("📥 Intercepting {} {}", method, requestURI);

        // ✅ Public routes (no authentication needed)
        if (isPublicRoute(requestURI, method)) {
            logger.info("🔓 Accès public autorisé : {} {}", method, requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);

            if (jwt == null) {
                logger.warn("⛔ Aucun token JWT trouvé");
                sendUnauthorizedError(response, "Authentication required");
                return;
            }

            if (!jwtUtils.validateJwtToken(jwt)) {
                logger.warn("⛔ Token JWT invalide");
                sendUnauthorizedError(response, "Invalid token");
                return;
            }

            String email = jwtUtils.getUserNameFromJwtToken(jwt);
            UserDetails userDetails = subDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("🔐 Utilisateur authentifié : {}", email);

        } catch (ExpiredJwtException ex) {
            logger.error("⌛ Token expiré : {}", ex.getMessage());
            sendUnauthorizedError(response, "Token expired");
            return;
        } catch (UsernameNotFoundException ex) {
            logger.error("🔍 Utilisateur non trouvé : {}", ex.getMessage());
            sendUnauthorizedError(response, "User not found");
            return;
        } catch (Exception ex) {
            logger.error("❌ Erreur d'authentification : {}", ex.getMessage());
            sendUnauthorizedError(response, "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicRoute(String uri, String method) {
        // Only allow POST to /api/associations (create new association) without auth
        return uri.startsWith("/api/auth/") ||

                (uri.equals("/api/associations") && "POST".equalsIgnoreCase(method))
                ||
                (uri.startsWith( "/api/donations/cagnotte") && "GET".equals(method)) ||

                (uri.startsWith("/api/paiments/create-payment-intent") && "POST".equals(method)) ||
                (uri.startsWith("/api/associations") && "POST".equals(method)) ||
                (uri.startsWith("/api/donations/getall") && "GET".equals(method)) ||
                (uri.startsWith("/api/donations/get/") && "GET".equals(method)) ||
                (uri.startsWith("/api/donations/find/") && "GET".equals(method)) ||
                (uri.startsWith("/api/dons/") && uri.endsWith("/contribute") && "POST".equals(method))

                ;
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{ \"error\": \"" + message + "\" }");
    }
}