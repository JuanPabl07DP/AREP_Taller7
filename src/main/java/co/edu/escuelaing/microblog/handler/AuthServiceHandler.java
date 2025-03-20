package co.edu.escuelaing.microblog.handler;

import co.edu.escuelaing.microblog.MicroblogApplication;
import co.edu.escuelaing.microblog.dto.ApiResponse;
import co.edu.escuelaing.microblog.dto.JwtAuthenticationResponse;
import co.edu.escuelaing.microblog.dto.LoginRequest;
import co.edu.escuelaing.microblog.dto.SignUpRequest;
import co.edu.escuelaing.microblog.model.User;
import co.edu.escuelaing.microblog.security.JwtTokenProvider;
import co.edu.escuelaing.microblog.service.UserService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthServiceHandler implements RequestStreamHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ApplicationContext context;
    private static final UserService userService;
    private static final JwtTokenProvider tokenProvider;

    // Inicializar el contexto de Spring al cargar la clase
    static {
        try {
            SpringApplication application = new SpringApplication(MicroblogApplication.class);
            application.setWebApplicationType(WebApplicationType.NONE);
            context = application.run();
            userService = context.getBean(UserService.class);
            tokenProvider = context.getBean(JwtTokenProvider.class);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing Spring context: " + e.getMessage(), e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        // Leer el evento entrante
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String requestBody = reader.lines().collect(Collectors.joining());

        // Convertir el cuerpo de la solicitud a JSON
        JsonNode event = objectMapper.readTree(requestBody);

        // Extraer información de la solicitud
        String path = event.path("path").asText();
        String httpMethod = event.path("httpMethod").asText();
        String body = event.path("body").asText();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization");

        String responseBody;
        int statusCode;

        try {
            // Procesar solicitudes de registro
            if (httpMethod.equals("POST") && path.equals("/api/auth/signup")) {
                SignUpRequest signUpRequest = objectMapper.readValue(body, SignUpRequest.class);

                // Verificar si el usuario ya existe
                if (userService.existsByUsername(signUpRequest.getUsername())) {
                    ApiResponse response = new ApiResponse(false, "Username is already taken!");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 400;
                } else if (userService.existsByEmail(signUpRequest.getEmail())) {
                    ApiResponse response = new ApiResponse(false, "Email Address already in use!");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 400;
                } else {
                    // Crear usuario
                    User user = new User();
                    user.setUsername(signUpRequest.getUsername());
                    user.setEmail(signUpRequest.getEmail());
                    user.setPassword(signUpRequest.getPassword());

                    userService.createUser(user);

                    ApiResponse response = new ApiResponse(true, "User registered successfully");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 201;
                }
            }
            // Procesar solicitudes de inicio de sesión
            else if (httpMethod.equals("POST") && path.equals("/api/auth/signin")) {
                LoginRequest loginRequest = objectMapper.readValue(body, LoginRequest.class);

                // Autenticar usuario
                User user = userService.getUserByUsername(loginRequest.getUsername());

                // Verificar contraseña (se debería usar un PasswordEncoder real en producción)
                if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
                    // Generar token
                    String jwt = tokenProvider.generateToken(user.getUsername());
                    JwtAuthenticationResponse response = new JwtAuthenticationResponse(jwt);
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 200;
                } else {
                    ApiResponse response = new ApiResponse(false, "Invalid username or password");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 401;
                }
            }
            // Manejar método OPTIONS para CORS preflight
            else if (httpMethod.equals("OPTIONS")) {
                responseBody = "";
                statusCode = 200;
            }
            // Manejar rutas no encontradas
            else {
                ApiResponse response = new ApiResponse(false, "Path not found: " + path);
                responseBody = objectMapper.writeValueAsString(response);
                statusCode = 404;
            }
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(false, "Error processing request: " + e.getMessage());
            responseBody = objectMapper.writeValueAsString(response);
            statusCode = 500;
        }

        // Crear respuesta
        Map<String, Object> lambdaResponse = new HashMap<>();
        lambdaResponse.put("statusCode", statusCode);
        lambdaResponse.put("headers", headers);
        lambdaResponse.put("body", responseBody);

        // Escribir la respuesta
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        writer.write(objectMapper.writeValueAsString(lambdaResponse));
        writer.close();
    }
}