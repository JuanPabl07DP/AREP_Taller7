package co.edu.escuelaing.microblog.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class JwtTokenProviderTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtConfig.getSecret()).thenReturn("thisIsAReallyLongSecretKeyWithAtLeast512BitsForHS512AlgorithmToMakeTheTestPass12345678901234567890123456789012345678901234");
        when(jwtConfig.getExpirationMs()).thenReturn(3600000L); // 1 hora
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Arrange
        UserDetails userDetails = new User("testuser", "password", new ArrayList<>());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, new ArrayList<>());

        // Act
        String token = tokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("testuser", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        // Arrange
        String token = tokenProvider.generateToken("testuser");

        // Act
        String username = tokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = tokenProvider.generateToken("testuser");

        // Act & Assert
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.string";

        // Act & Assert
        assertFalse(tokenProvider.validateToken(invalidToken));
    }

    @Test
    void getClaimFromToken_ShouldReturnCorrectClaim() {
        // Arrange
        String token = tokenProvider.generateToken("testuser");

        // Act
        String subject = tokenProvider.getClaimFromToken(token, Claims::getSubject);

        // Assert
        assertEquals("testuser", subject);
    }
}