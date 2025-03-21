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
import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import co.edu.escuelaing.microblog.MicroblogApplication;

@Service
public class PostService {

    private static final ApplicationContext context;

    // Inicializar el contexto de Spring si es necesario
    static {
        ApplicationContext tempContext = null;
        try {
            SpringApplication application = new SpringApplication(MicroblogApplication.class);
            application.setWebApplicationType(WebApplicationType.NONE);
            tempContext = application.run();
        } catch (Exception e) {
            System.err.println("Error initializing Spring context: " + e.getMessage());
        }
        context = tempContext;
    }

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreamRepository streamRepository;

    // Constructor para inicialización manual
    public PostService() {
        try {
            if (postRepository == null && context != null) {
                this.postRepository = context.getBean(PostRepository.class);
            }
            if (userRepository == null && context != null) {
                this.userRepository = context.getBean(UserRepository.class);
            }
            if (streamRepository == null && context != null) {
                this.streamRepository = context.getBean(StreamRepository.class);
            }
        } catch (Exception e) {
            System.err.println("Error getting beans from context: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los posts con paginación
     */
    public Page<Post> getAllPosts(Pageable pageable) {
        if (postRepository == null) {
            throw new IllegalStateException("PostRepository not initialized");
        }
        return postRepository.findAll(pageable);
    }

    /**
     * Obtiene un post por su ID
     */
    public Post getPostById(Long id) {
        if (postRepository == null) {
            throw new IllegalStateException("PostRepository not initialized");
        }
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
    }

    /**
     * Obtiene posts por stream con paginación
     */
    public Page<Post> getPostsByStream(Long streamId, Pageable pageable) {
        if (postRepository == null || streamRepository == null) {
            throw new IllegalStateException("Repositories not initialized");
        }

        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream", "id", streamId));

        return postRepository.findByStream(stream, pageable);
    }

    /**
     * Obtiene posts por usuario con paginación
     */
    public Page<Post> getPostsByUser(Long userId, Pageable pageable) {
        if (postRepository == null || userRepository == null) {
            throw new IllegalStateException("Repositories not initialized");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return postRepository.findByUser(user, pageable);
    }

    /**
     * Crea un nuevo post
     */
    @Transactional
    public Post createPost(Post post, Long userId, Long streamId) {
        if (postRepository == null || userRepository == null || streamRepository == null) {
            throw new IllegalStateException("Repositories not initialized");
        }

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
        if (postRepository == null) {
            throw new IllegalStateException("PostRepository not initialized");
        }

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
        if (postRepository == null) {
            throw new IllegalStateException("PostRepository not initialized");
        }

        Post post = getPostById(id);
        postRepository.delete(post);
    }
}