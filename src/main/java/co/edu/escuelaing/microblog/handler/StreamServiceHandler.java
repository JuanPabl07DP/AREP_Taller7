package co.edu.escuelaing.microblog.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import co.edu.escuelaing.microblog.dto.ApiResponse;
import co.edu.escuelaing.microblog.model.Stream;
import co.edu.escuelaing.microblog.security.JwtTokenProvider;
import co.edu.escuelaing.microblog.service.StreamService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StreamServiceHandler implements RequestStreamHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final StreamService streamService = new StreamService();
    private static final JwtTokenProvider tokenProvider = new JwtTokenProvider();

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
        JsonNode headers = event.path("headers");

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

            // Obtener todos los streams
            if (httpMethod.equals("GET") && path.equals("/api/streams")) {
                List<Stream> streams = streamService.getAllStreams();
                responseBody = objectMapper.writeValueAsString(streams);
                statusCode = 200;
            }
            // Obtener un stream por ID
            else if (httpMethod.equals("GET") && path.matches("/api/streams/\\d+")) {
                // Extraer el ID del stream de la URL
                Pattern pattern = Pattern.compile("/api/streams/(\\d+)");
                Matcher matcher = pattern.matcher(path);

                if (matcher.find()) {
                    Long streamId = Long.parseLong(matcher.group(1));
                    Stream stream = streamService.getStreamById(streamId);

                    responseBody = objectMapper.writeValueAsString(stream);
                    statusCode = 200;
                } else {
                    ApiResponse response = new ApiResponse(false, "Invalid stream ID format");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 400;
                }
            }
            // Obtener un stream por nombre
            else if (httpMethod.equals("GET") && path.startsWith("/api/streams/name/")) {
                // Extraer el nombre del stream de la URL
                String streamName = path.substring("/api/streams/name/".length());
                Stream stream = streamService.getStreamByName(streamName);

                responseBody = objectMapper.writeValueAsString(stream);
                statusCode = 200;
            }
            // Crear un nuevo stream
            else if (httpMethod.equals("POST") && path.equals("/api/streams")) {
                // Verificar token JWT
                if (authToken.isEmpty() || !tokenProvider.validateToken(authToken)) {
                    ApiResponse response = new ApiResponse(false, "Unauthorized");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 401;
                } else {
                    // Convertir el cuerpo de la solicitud a un objeto Stream
                    Stream stream = objectMapper.readValue(body, Stream.class);

                    // Crear el stream
                    Stream createdStream = streamService.createStream(stream);

                    responseBody = objectMapper.writeValueAsString(createdStream);
                    statusCode = 201;
                }
            }
            // Actualizar un stream existente
            else if (httpMethod.equals("PUT") && path.matches("/api/streams/\\d+")) {
                // Verificar token JWT
                if (authToken.isEmpty() || !tokenProvider.validateToken(authToken)) {
                    ApiResponse response = new ApiResponse(false, "Unauthorized");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 401;
                } else {
                    // Extraer el ID del stream de la URL
                    Pattern pattern = Pattern.compile("/api/streams/(\\d+)");
                    Matcher matcher = pattern.matcher(path);

                    if (matcher.find()) {
                        Long streamId = Long.parseLong(matcher.group(1));

                        // Convertir el cuerpo de la solicitud a un objeto Stream
                        Stream stream = objectMapper.readValue(body, Stream.class);

                        // Actualizar el stream
                        Stream updatedStream = streamService.updateStream(streamId, stream);

                        responseBody = objectMapper.writeValueAsString(updatedStream);
                        statusCode = 200;
                    } else {
                        ApiResponse response = new ApiResponse(false, "Invalid stream ID format");
                        responseBody = objectMapper.writeValueAsString(response);
                        statusCode = 400;
                    }
                }
            }
            // Eliminar un stream
            else if (httpMethod.equals("DELETE") && path.matches("/api/streams/\\d+")) {
                // Verificar token JWT
                if (authToken.isEmpty() || !tokenProvider.validateToken(authToken)) {
                    ApiResponse response = new ApiResponse(false, "Unauthorized");
                    responseBody = objectMapper.writeValueAsString(response);
                    statusCode = 401;
                } else {
                    // Extraer el ID del stream de la URL
                    Pattern pattern = Pattern.compile("/api/streams/(\\d+)");
                    Matcher matcher = pattern.matcher(path);

                    if (matcher.find()) {
                        Long streamId = Long.parseLong(matcher.group(1));

                        // Eliminar el stream
                        streamService.deleteStream(streamId);

                        ApiResponse response = new ApiResponse(true, "Stream deleted successfully");
                        responseBody = objectMapper.writeValueAsString(response);
                        statusCode = 200;
                    } else {
                        ApiResponse response = new ApiResponse(false, "Invalid stream ID format");
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
}