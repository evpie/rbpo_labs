package com.example.lab2.service;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/auth/login") || path.equals("/auth/register") || path.equals("/auth/refresh")
                || path.equals("/api/auth/login") || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String username = jwtService.validateAccessToken(header.substring(7)).getSubject();
                UserDetails user = userDetailsService.loadUserByUsername(username);
                if (!user.isEnabled()) {
                    throw new UsernameNotFoundException("Учетная запись заблокирована");
                }
                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
                SecurityContextHolder.clearContext();
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Неверный access-токен\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
