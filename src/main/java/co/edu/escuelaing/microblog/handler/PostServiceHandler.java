package co.edu.escuelaing.microblog.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import co.edu.escuelaing.microblog.dto.ApiResponse;
import co.edu.escuelaing.microblog.model.Post;
import co.edu.escuelaing.microblog.security.JwtTokenProvider;
import co.edu.escuelaing.microblog.service.PostService;
import co.edu.escuelaing.microblog.service.StreamService;
import co.edu.escuelaing.microblog.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PostServiceHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final PostService postService = new PostService();
    private static final UserService userService = new UserService();
    private static final StreamService streamService = new StreamService();
    private static final JwtTokenProvider tokenProvider = new JwtTokenProvider();

    public static void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        // Leer el evento entrante
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String requestBody = reader.lines().collect(Collectors.joining());

        // Convertir el cuerpo de la solicitud a JSON
        JsonNode event = objectMapper.readTree(requestBody);

        // Extraer información de la solicitud
        String path = event.path("path").asText();
        String httpMethod = event.path("httpMethod").asText();
        String body = event.path("body").asText();
        JsonNode headers = event.path("headers");
        JsonNode queryStringParameters = event.path("queryStringParameters");

        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("Content-Type", "application/json");
        responseHeaders.put("Access-Control-Allow-Origin", "*");
        responseHeaders.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        responseHeaders.put("Access-Control-Allow-Headers", "Content-Type, Authorization");

        String responseBody;
        int statusCode;

        try {
            // Verificar autorización para operaciones que requieren autenticación
            String authToken = headers.path("Authorization").asText("").replace("Bearer ", "");

            // Obtener todos los posts
            if (httpMethod.equals("GET") && path.equals("/api/posts")) {
                // Extraer parámetros de paginación
                int page = queryStringParameters != null && !queryStringParameters.path("page").isMissingNode() ?
                        queryStringParameters.path("page").asInt(0) : 0;
                int size = queryStringParameters != null && !queryStringParameters.path("size").isMissingNode() ?
                        queryStringParameters.path("size").asInt(10) : 10;

                Pageable pageable = PageRequest.of(page, size);
                Page<Post> posts = postService.getAllPosts(pageable);

                responseBody = objectMapper.writeValueAsString(posts);
                statusCode = 200;
            }
            // Obtener posts por stream
            else if (httpMethod.equals("GET") && path.matches("/api/posts/stream/\\d+")) {
                // Extraer el ID del stream de la URL
                Pattern pattern = Pattern.compile("/api/posts/stream/(\\d+)");
                Matcher matcher = pattern.matcher(path);

                if (matcher.find()) {
                    Long streamId = Long.parseLong(matcher.group(1));

                    // Extraer parámetros de paginación
                    int page = queryStringParameters != null && !queryStringParameters.path("page").isMissingNode() ?
                            queryStringParameters.path("page").asInt(0) : 0;
                    int size = queryStringParameters != null && !queryStringParameters.path("size").isMissingNode() ?
                            queryStringParameters.path("size").asInt(10) : 10;

                    Pageable pageable = PageRequest.of(page, size);
                    Page<Post> posts = postService.getPostsByStream(streamId, pageable);

                    responseBody = objectMapper.writeValueAsString(posts);
                    statusCode = 200;
                } else {
                    ApiResponse response = new ApiResponse(false, "Invalid stream ID format");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 400;
                }
            }
            // Obtener posts por usuario
            else if (httpMethod.equals("GET") && path.matches("/api/posts/user/\\d+")) {
                // Extraer el ID del usuario de la URL
                Pattern pattern = Pattern.compile("/api/posts/user/(\\d+)");
                Matcher matcher = pattern.matcher(path);

                if (matcher.find()) {
                    Long userId = Long.parseLong(matcher.group(1));

                    // Extraer parámetros de paginación
                    int page = queryStringParameters != null && !queryStringParameters.path("page").isMissingNode() ?
                            queryStringParameters.path("page").asInt(0) : 0;
                    int size = queryStringParameters != null && !queryStringParameters.path("size").isMissingNode() ?
                            queryStringParameters.path("size").asInt(10) : 10;

                    Pageable pageable = PageRequest.of(page, size);
                    Page<Post> posts = postService.getPostsByUser(userId, pageable);

                    responseBody = objectMapper.writeValueAsString(posts);
                    statusCode = 200;
                } else {
                    ApiResponse response = new ApiResponse(false, "Invalid user ID format");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 400;
                }
            }
            // Crear un nuevo post
            else if (httpMethod.equals("POST") && path.matches("/api/posts/user/\\d+/stream/\\d+")) {
                // Verificar token JWT
                if (authToken.isEmpty() || !tokenProvider.validateToken(authToken)) {
                    ApiResponse response = new ApiResponse(false, "Unauthorized");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 401;
                } else {
                    // Extraer los IDs de usuario y stream de la URL
                    Pattern pattern = Pattern.compile("/api/posts/user/(\\d+)/stream/(\\d+)");
                    Matcher matcher = pattern.matcher(path);

                    if (matcher.find()) {
                        Long userId = Long.parseLong(matcher.group(1));
                        Long streamId = Long.parseLong(matcher.group(2));

                        // Convertir el cuerpo de la solicitud a un objeto Post
                        Post post = objectMapper.readValue(body, Post.class);

                        // Crear el post
                        Post createdPost = postService.createPost(post, userId, streamId);

                        responseBody = objectMapper.writeValueAsString(createdPost);
                        statusCode = 201;
                    } else {
                        ApiResponse response = new ApiResponse(false, "Invalid URL format");
                        responseBody = objectMapper.writeValueAsString(response);
                        statusCode = 400;
                    }
                }
            }
            // Actualizar un post existente
            else if (httpMethod.equals("PUT") && path.matches("/api/posts/\\d+")) {
                // Verificar token JWT
                if (authToken.isEmpty() || !tokenProvider.validateToken(authToken)) {
                    ApiResponse response = new ApiResponse(false, "Unauthorized");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 401;
                } else {
                    // Extraer el ID del post de la URL
                    Pattern pattern = Pattern.compile("/api/posts/(\\d+)");
                    Matcher matcher = pattern.matcher(path);

                    if (matcher.find()) {
                        Long postId = Long.parseLong(matcher.group(1));

                        // Convertir el cuerpo de la solicitud a un objeto Post
                        Post post = objectMapper.readValue(body, Post.class);

                        // Actualizar el post
                        Post updatedPost = postService.updatePost(postId, post);

                        responseBody = objectMapper.writeValueAsString(updatedPost);
                        statusCode = 200;
                    } else {
                        ApiResponse response = new ApiResponse(false, "Invalid post ID format");
                        responseBody = objectMapper.writeValueAsString(response);
                        statusCode = 400;
                    }
                }
            }
            // Eliminar un post
            else if (httpMethod.equals("DELETE") && path.matches("/api/posts/\\d+")) {
                // Verificar token JWT
                if (authToken.isEmpty() || !tokenProvider.validateToken(authToken)) {
                    ApiResponse response = new ApiResponse(false, "Unauthorized");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 401;
                } else {
                    // Extraer el ID del post de la URL
                    Pattern pattern = Pattern.compile("/api/posts/(\\d+)");
                    Matcher matcher = pattern.matcher(path);

                    if (matcher.find()) {
                        Long postId = Long.parseLong(matcher.group(1));

                        // Eliminar el post
                        postService.deletePost(postId);

                        ApiResponse response = new ApiResponse(true, "Post deleted successfully");
                        responseBody = objectMapper.writeValueAsString(response);
                        statusCode = 200;
                    } else {
                        ApiResponse response = new ApiResponse(false, "Invalid post ID format");
                        responseBody = objectMapper.writeValueAsString(response);
                        statusCode = 400;
                    }
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
        lambdaResponse.put("headers", responseHeaders);
        lambdaResponse.put("body", responseBody);

        // Escribir la respuesta
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        writer.write(objectMapper.writeValueAsString(lambdaResponse));
        writer.close();
    }

    // Clase interna para representar el contexto de Lambda
    public static class Context {
        private String functionName;

        public String getFunctionName() {
            return functionName;
        }

        public void setFunctionName(String functionName) {
            this.functionName = functionName;
        }
    }
}