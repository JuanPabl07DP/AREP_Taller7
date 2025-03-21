package co.edu.escuelaing.microblog.service;

import co.edu.escuelaing.microblog.model.Post;
import co.edu.escuelaing.microblog.model.Stream;
import co.edu.escuelaing.microblog.model.User;
import co.edu.escuelaing.microblog.repository.PostRepository;
import co.edu.escuelaing.microblog.repository.StreamRepository;
import co.edu.escuelaing.microblog.repository.UserRepository;
import co.edu.escuelaing.microblog.exception.ResourceNotFoundException;
import co.edu.escuelaing.microblog.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreamRepository streamRepository;

    /**
     * Obtiene todos los posts con paginación
     */
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    /**
     * Obtiene un post por su ID
     */
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
    }

    /**
     * Obtiene posts por stream con paginación
     */
    public Page<Post> getPostsByStream(Long streamId, Pageable pageable) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", "id", streamId));

        return postRepository.findByStream(stream, pageable);
    }

    /**
     * Obtiene posts por usuario con paginación
     */
    public Page<Post> getPostsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return postRepository.findByUser(user, pageable);
    }

    /**
     * Crea un nuevo post
     */
    @Transactional
    public Post createPost(Post post, Long userId, Long streamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", "id", streamId));

        // Validar longitud del contenido (máximo 140 caracteres)
        if (post.getContent() == null || post.getContent().isEmpty()) {
            throw new BadRequestException("Post content cannot be empty");
        }

        if (post.getContent().length() > 140) {
            throw new BadRequestException("Post content exceeds 140 characters limit");
        }

        post.setUser(user);
        post.setStream(stream);

        return postRepository.save(post);
    }

    /**
     * Actualiza un post existente
     */
    @Transactional
    public Post updatePost(Long id, Post postDetails) {
        Post post = getPostById(id);

        // Validar longitud del contenido (máximo 140 caracteres)
        if (postDetails.getContent() == null || postDetails.getContent().isEmpty()) {
            throw new BadRequestException("Post content cannot be empty");
        }

        if (postDetails.getContent().length() > 140) {
            throw new BadRequestException("Post content exceeds 140 characters limit");
        }

        post.setContent(postDetails.getContent());

        return postRepository.save(post);
    }

    /**
     * Elimina un post
     */
    @Transactional
    public void deletePost(Long id) {
        Post post = getPostById(id);
        postRepository.delete(post);
    }
}