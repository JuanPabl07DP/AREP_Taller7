package co.edu.escuelaing.microblog.service;

import co.edu.escuelaing.microblog.model.User;
import co.edu.escuelaing.microblog.repository.UserRepository;
import co.edu.escuelaing.microblog.exception.ResourceNotFoundException;
import co.edu.escuelaing.microblog.exception.ResourceAlreadyExistsException;
import co.edu.escuelaing.microblog.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Obtiene un usuario por su ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Obtiene un usuario por su nombre de usuario
     */
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Crea un nuevo usuario
     */
    @Transactional
    public User createUser(User user) {
        // Validar que el usuario tenga un nombre de usuario
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }

        // Validar que el usuario tenga un email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }

        // Validar que el usuario tenga una contraseña
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }

        // Verificar si el nombre de usuario ya existe
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResourceAlreadyExistsException("User", "username", user.getUsername());
        }

        // Verificar si el email ya existe
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", user.getEmail());
        }

        // Codificar la contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    /**
     * Actualiza un usuario existente
     */
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);

        // Validar que el usuario tenga un nombre de usuario
        if (userDetails.getUsername() == null || userDetails.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }

        // Validar que el usuario tenga un email
        if (userDetails.getEmail() == null || userDetails.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }

        // Verificar si el nuevo nombre de usuario ya existe (si está cambiando el nombre de usuario)
        if (!user.getUsername().equals(userDetails.getUsername()) &&
                userRepository.existsByUsername(userDetails.getUsername())) {
            throw new ResourceAlreadyExistsException("User", "username", userDetails.getUsername());
        }

        // Verificar si el nuevo email ya existe (si está cambiando el email)
        if (!user.getEmail().equals(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", userDetails.getEmail());
        }

        // Actualizar campos
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());

        // Solo actualizar la contraseña si se proporciona una nueva
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * Elimina un usuario
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}