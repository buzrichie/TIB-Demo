package com.amalitech.tib.security.filter;


import com.amalitech.tib.authentication.service.TokenBlacklistService;
import com.amalitech.tib.exception.InvalidTokenException;
import com.amalitech.tib.security.CustomUserDetailsService;
import com.amalitech.tib.security.provider.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                throw new InvalidTokenException("Missing or invalid Authorization header");
            }

            String token = header.substring(7);

            if (!jwtTokenProvider.validateToken(token)) {
                filterChain.doFilter(request, response);
                throw new InvalidTokenException("Invalid token");
            }

            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                filterChain.doFilter(request, response);
                throw new InvalidTokenException("Token has been blacklisted");
            }

            String userId = jwtTokenProvider.getSubject(token);
            if (userId == null) {
                filterChain.doFilter(request, response);
                throw new InvalidTokenException("Invalid token: missing subject");
            }

            var userDetails = customUserDetailsService.loadUserById(userId);

            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token for request: {}", request.getRequestURI());
        } catch (Exception ex) {
            log.error("Error authenticating request: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
