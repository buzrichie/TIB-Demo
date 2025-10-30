package com.amalitech.tib.user.security.provider;

import com.amalitech.tib.exception.BadException;
import com.amalitech.tib.exception.InvalidTokenException;
import com.amalitech.tib.exception.TokenRequiredException;
import com.amalitech.tib.user.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration:900000}")
  private long accessTokenExpiration;

  @Value("${jwt.refresh-expiration:604800000}")
  private long refreshTokenExpiration;

  private Key key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  public String generateAccessToken(User user) {
    List<String> roles =
        user.getEffectiveRoles().stream().map(role -> role.getName().toUpperCase()).toList();

    List<String> permissions =
        user.getEffectiveRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Enum::name)
            .toList();

    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", roles);
    claims.put("permissions", permissions);
    claims.put("email", user.getEmail());

    return generateToken(user.getId().toString(), claims, accessTokenExpiration);
  }

  public String generateRefreshToken(String userId) {
    return generateToken(userId, null, refreshTokenExpiration);
  }

  private String generateToken(String userId, Map<String, Object> claims, long expirationTime) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationTime);

    JwtBuilder builder =
        Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS256);

    if (claims != null && !claims.isEmpty()) {
      builder.addClaims(claims);
    }

    return builder.compact();
  }

  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }

  public Claims parseClaims(String token) {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    } catch (ExpiredJwtException ex) {
      throw new InvalidTokenException("Invalid JWT Token Expired");
    } catch (JwtException e) {
      throw new InvalidTokenException("Invalid JWT token structure or signature.");
    } catch (Exception e) {
      throw new BadException("An unexpected error occurred while processing the token.");
    }
  }

  public String getSubject(String token) {
    return parseClaims(token).getSubject();
  }

  public List<String> getRoles(String token) {
    Object roles = parseClaims(token).get("roles");
    if (roles instanceof List<?> list) {
      return list.stream().map(String::valueOf).toList();
    }
    return List.of();
  }

  public List<String> getPermissions(String token) {
    Object permissions = parseClaims(token).get("permissions");
    if (permissions instanceof List<?> list) {
      return list.stream().map(String::valueOf).toList();
    }
    return List.of();
  }

  public String getEmail(String token) {
    return (String) parseClaims(token).get("email");
  }

  public Instant getExpiration(String token) {
    Instant expiration = parseClaims(token).getExpiration().toInstant();
    if (Instant.now().isAfter(expiration)) {
      throw new InvalidTokenException("Token has expired");
    }
    return expiration;
  }

  public String extractJWTAuthTokenFromHeader(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      throw new TokenRequiredException("Token is required");
    }

    return header.substring(7);
  }
}
