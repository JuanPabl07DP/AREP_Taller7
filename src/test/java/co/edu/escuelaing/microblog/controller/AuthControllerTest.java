package co.edu.escuelaing.microblog.controller;

import co.edu.escuelaing.microblog.dto.ApiResponse;
import co.edu.escuelaing.microblog.dto.JwtAuthenticationResponse;
import co.edu.escuelaing.microblog.dto.LoginRequest;
import co.edu.escuelaing.microblog.dto.SignUpRequest;
import co.edu.escuelaing.microblog.model.User;
import co.edu.escuelaing.microblog.repository.UserRepository;
import co.edu.escuelaing.microblog.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import static org.mockito.Mockito.mockStatic;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private SignUpRequest signUpRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setEmail("new@example.com");
        signUpRequest.setPassword("password");

        authentication = mock(Authentication.class);
    }

    @Test
    void authenticateUser_ShouldReturnJwtToken() {
        // Arrange
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof JwtAuthenticationResponse);
        assertEquals("jwt-token", ((JwtAuthenticationResponse) response.getBody()).getAccessToken());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(tokenProvider, times(1)).generateToken(any(Authentication.class));
    }

    @Test
    void registerUser_WithExistingUsername_ShouldReturnBadRequest() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiResponse);
        assertFalse(((ApiResponse) response.getBody()).getSuccess());
        assertEquals("Username is already taken!", ((ApiResponse) response.getBody()).getMessage());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldReturnBadRequest() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiResponse);
        assertFalse(((ApiResponse) response.getBody()).getSuccess());
        assertEquals("Email Address already in use!", ((ApiResponse) response.getBody()).getMessage());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithValidRequest_ShouldReturnCreated() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("new@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Mockear el ServletUriComponentsBuilder
        MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class);
        ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);

        when(ServletUriComponentsBuilder.fromCurrentContextPath()).thenReturn(builder);
        when(builder.path(anyString())).thenReturn(builder);
        when(builder.buildAndExpand(Optional.ofNullable(any()))).thenReturn(UriComponentsBuilder.fromPath("/api/users/newuser").build());

        try {
            // Act
            ResponseEntity<?> response = authController.registerUser(signUpRequest);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue(response.getBody() instanceof ApiResponse);
            assertTrue(((ApiResponse) response.getBody()).getSuccess());
            assertEquals("User registered successfully", ((ApiResponse) response.getBody()).getMessage());
            verify(userRepository, times(1)).save(any(User.class));
            verify(passwordEncoder, times(1)).encode(anyString());
        } finally {
            mockedBuilder.close();
        }
    }
}