package com.amalitech.tib.user.security.filter;

import com.amalitech.tib.user.security.provider.JwtTokenProvider;
import com.amalitech.tib.user.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final TokenBlacklistService tokenBlacklistService;

  // Endpoints to skip authentication for
  private static final List<String> EXCLUDED_SEGMENTS =
      List.of("/auth/login", "/auth/logout", "/auth/register", "/auth/refresh-token");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestPath = request.getRequestURI();

    if (isExcludedPath(requestPath)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String header = request.getHeader("Authorization");
      if (header == null || !header.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
      }

      String token = header.substring(7);

      if (!jwtTokenProvider.validateToken(token)
          || tokenBlacklistService.isTokenBlacklisted(token)) {
        filterChain.doFilter(request, response);
        return;
      }

      String userId = jwtTokenProvider.getSubject(token);
      String email = jwtTokenProvider.getEmail(token);
      List<String> roles = jwtTokenProvider.getRoles(token);
      List<String> permissions = jwtTokenProvider.getPermissions(token);

      var authorities = new HashSet<SimpleGrantedAuthority>();
      roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
      permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));

      var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (ExpiredJwtException ex) {
      log.info("JWT expired for request: {}", request.getRequestURI());
    } catch (Exception ex) {
      log.error("Error authenticating request: {}", ex.getMessage(), ex);
    }

    filterChain.doFilter(request, response);
  }

  private boolean isExcludedPath(String path) {
    String normalized = path.replaceAll("^/api/v\\d+", "");
    return EXCLUDED_SEGMENTS.stream().anyMatch(normalized::startsWith);
  }
}
