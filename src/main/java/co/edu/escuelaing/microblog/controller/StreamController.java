package co.edu.escuelaing.microblog.controller;

import co.edu.escuelaing.microblog.model.Stream;
import co.edu.escuelaing.microblog.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/streams")
public class StreamController {

    @Autowired
    private StreamService streamService;

    /**
     * Obtiene todos los streams
     */
    @GetMapping
    public List<Stream> getAllStreams() {
        return streamService.getAllStreams();
    }

    /**
     * Obtiene un stream específico por su ID
     */
    @GetMapping("/{id}")
    public Stream getStreamById(@PathVariable Long id) {
        return streamService.getStreamById(id);
    }

    /**
     * Obtiene un stream por su nombre
     */
    @GetMapping("/name/{name}")
    public Stream getStreamByName(@PathVariable String name) {
        return streamService.getStreamByName(name);
    }

    /**
     * Crea un nuevo stream
     */
    @PostMapping
    public Stream createStream(@Valid @RequestBody Stream stream) {
        return streamService.createStream(stream);
    }

    /**
     * Actualiza un stream existente
     */
    @PutMapping("/{id}")
    public Stream updateStream(@PathVariable Long id, @Valid @RequestBody Stream stream) {
        return streamService.updateStream(id, stream);
    }

    /**
     * Elimina un stream existente
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStream(@PathVariable Long id) {
        streamService.deleteStream(id);
        return ResponseEntity.ok().build();
    }
}