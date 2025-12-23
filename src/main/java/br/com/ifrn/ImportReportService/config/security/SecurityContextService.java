package br.com.ifrn.ImportReportService.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextService {
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            // 1. Se logado via Navegador (OidcUser)
            if (principal instanceof OidcUser oidc) {
                return  oidc.getSubject(); // Pega o 'sub'
            }
            else if (principal instanceof Jwt jwt) {
                return jwt.getSubject(); // Pega o 'sub'
            }
        }
        throw new IllegalStateException("Usuário não autenticado ou token inválido");
    }
}
