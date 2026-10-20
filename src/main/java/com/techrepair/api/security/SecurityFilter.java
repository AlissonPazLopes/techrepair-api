package com.techrepair.api.security;

import com.techrepair.api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = recuperarToken(request);
        if (token != null) {
            String email = tokenService.validarToken(token);
            if (email != null) {
                usuarioRepository.findByEmail(email)
                        .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                        .ifPresent(usuario -> {
                            var authentication = new UsernamePasswordAuthenticationToken(
                                    usuario, null, usuario.getAuthorities()
                            );
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7).trim();
    }
}
