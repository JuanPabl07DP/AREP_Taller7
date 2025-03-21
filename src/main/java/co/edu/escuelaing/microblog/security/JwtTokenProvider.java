package co.edu.escuelaing.microblog.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String DEFAULT_SECRET = "jwtsecretkey9876543210abcdefghijklmnopqrstuvwxyzJWTSUPERSECRETKEY";
    private static final long DEFAULT_EXPIRATION_MS = 86400000; // 24 horas

    @Autowired
    private JwtConfig jwtConfig;

    // Generate a token for a user
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername());
    }

    // Generate a token based on username
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + getExpirationMs());

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Get username from the token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.getSubject());
    }

    // Get any claim from token
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // Get all claims from token
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if token is valid
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        }
        return false;
    }

    // Get signing key
    private Key getSigningKey() {
        byte[] keyBytes = getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Métodos para obtener configuración con valores por defecto
    private String getSecret() {
        return (jwtConfig != null && jwtConfig.getSecret() != null)
                ? jwtConfig.getSecret()
                : DEFAULT_SECRET;
    }

    private long getExpirationMs() {
        return (jwtConfig != null)
                ? jwtConfig.getExpirationMs()
                : DEFAULT_EXPIRATION_MS;
    }
}