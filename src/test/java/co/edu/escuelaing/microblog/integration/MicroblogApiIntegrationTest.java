package co.edu.escuelaing.microblog.integration;

import co.edu.escuelaing.microblog.dto.JwtAuthenticationResponse;
import co.edu.escuelaing.microblog.dto.LoginRequest;
import co.edu.escuelaing.microblog.dto.SignUpRequest;
import co.edu.escuelaing.microblog.model.Post;
import co.edu.escuelaing.microblog.model.Stream;
import co.edu.escuelaing.microblog.model.User;
import co.edu.escuelaing.microblog.repository.PostRepository;
import co.edu.escuelaing.microblog.repository.StreamRepository;
import co.edu.escuelaing.microblog.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Disabled;


@SpringBootTest
@AutoConfigureMockMvc
@Disabled("Pruebas de integración deshabilitadas temporalmente")
public class MicroblogApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Limpiar los datos existentes
        postRepository.deleteAll();
        streamRepository.deleteAll();
        userRepository.deleteAll();

        // Registrar un usuario para pruebas
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("testuser");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        // Iniciar sesión para obtener token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JwtAuthenticationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), JwtAuthenticationResponse.class);
        authToken = response.getAccessToken();
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        streamRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateAndGetStream() throws Exception {
        // Crear un stream
        Stream newStream = new Stream();
        newStream.setName("TestStream");
        newStream.setDescription("Test description");

        mockMvc.perform(post("/api/streams")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStream)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("TestStream")));

        // Obtener todos los streams
        mockMvc.perform(get("/api/streams")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("TestStream")));
    }

    @Test
    void testCreateAndGetPost() throws Exception {
        // Crear un stream primero
        Stream newStream = new Stream();
        newStream.setName("TestStream");
        streamRepository.save(newStream);

        // Obtener el ID del usuario
        User user = userRepository.findByUsername("testuser").orElseThrow();

        // Crear un post
        Post newPost = new Post();
        newPost.setContent("Test post content");

        mockMvc.perform(post("/api/posts/user/" + user.getId() + "/stream/" + newStream.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", is("Test post content")))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.streamName", is("TestStream")));

        // Obtener todos los posts
        mockMvc.perform(get("/api/posts")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].content", is("Test post content")));
    }

    @Test
    void testUpdateUser() throws Exception {
        // Obtener el ID del usuario
        User user = userRepository.findByUsername("testuser").orElseThrow();

        // Actualizar usuario
        User updatedUser = new User();
        updatedUser.setUsername("testuser"); // Mantener el mismo username
        updatedUser.setEmail("updated@example.com");

        mockMvc.perform(put("/api/users/" + user.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("updated@example.com")));
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        // Intentar acceder a un recurso protegido sin autenticación
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}