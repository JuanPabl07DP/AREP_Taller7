package co.edu.escuelaing.microblog.service;

import co.edu.escuelaing.microblog.model.Stream;
import co.edu.escuelaing.microblog.repository.StreamRepository;
import co.edu.escuelaing.microblog.exception.ResourceNotFoundException;
import co.edu.escuelaing.microblog.exception.ResourceAlreadyExistsException;
import co.edu.escuelaing.microblog.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class StreamService {

    @Autowired
    private StreamRepository streamRepository;

    /**
     * Obtiene todos los streams
     */
    public List<Stream> getAllStreams() {
        return streamRepository.findAll();
    }

    /**
     * Obtiene un stream por su ID
     */
    public Stream getStreamById(Long id) {
        return streamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", "id", id));
    }

    /**
     * Obtiene un stream por su nombre
     */
    public Stream getStreamByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Stream name cannot be empty");
        }

        return streamRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", "name", name));
    }

    /**
     * Crea un nuevo stream
     */
    @Transactional
    public Stream createStream(Stream stream) {
        // Validar que el stream tenga un nombre
        if (stream.getName() == null || stream.getName().trim().isEmpty()) {
            throw new BadRequestException("Stream name cannot be empty");
        }

        // Verificar si el nombre ya existe
        if (streamRepository.existsByName(stream.getName())) {
            throw new ResourceAlreadyExistsException("Stream", "name", stream.getName());
        }

        // Validar longitud de la descripción
        if (stream.getDescription() != null && stream.getDescription().length() > 200) {
            throw new BadRequestException("Stream description exceeds 200 characters limit");
        }

        return streamRepository.save(stream);
    }

    /**
     * Actualiza un stream existente
     */
    @Transactional
    public Stream updateStream(Long id, Stream streamDetails) {
        Stream stream = getStreamById(id);

        // Validar que el stream tenga un nombre
        if (streamDetails.getName() == null || streamDetails.getName().trim().isEmpty()) {
            throw new BadRequestException("Stream name cannot be empty");
        }

        // Verificar si el nuevo nombre ya existe (si está cambiando el nombre)
        if (!stream.getName().equals(streamDetails.getName()) &&
                streamRepository.existsByName(streamDetails.getName())) {
            throw new ResourceAlreadyExistsException("Stream", "name", streamDetails.getName());
        }

        // Validar longitud de la descripción
        if (streamDetails.getDescription() != null && streamDetails.getDescription().length() > 200) {
            throw new BadRequestException("Stream description exceeds 200 characters limit");
        }

        // Actualizar campos
        stream.setName(streamDetails.getName());
        stream.setDescription(streamDetails.getDescription());

        return streamRepository.save(stream);
    }

    /**
     * Elimina un stream
     */
    @Transactional
    public void deleteStream(Long id) {
        Stream stream = getStreamById(id);
        streamRepository.delete(stream);
    }

    /**
     * Verifica si existe un stream con el nombre especificado
     */
    public boolean existsByName(String name) {
        return streamRepository.existsByName(name);
    }
}