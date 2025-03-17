package co.edu.escuelaing.microblog.controller;

import co.edu.escuelaing.microblog.model.Post;
import co.edu.escuelaing.microblog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * Obtiene todos los posts con paginación
     */
    @GetMapping
    public Page<Post> getAllPosts(Pageable pageable) {
        return postService.getAllPosts(pageable);
    }

    /**
     * Obtiene un post específico por su ID
     */
    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    /**
     * Obtiene posts por stream con paginación
     */
    @GetMapping("/stream/{streamId}")
    public Page<Post> getPostsByStream(@PathVariable Long streamId, Pageable pageable) {
        return postService.getPostsByStream(streamId, pageable);
    }

    /**
     * Obtiene posts por usuario con paginación
     */
    @GetMapping("/user/{userId}")
    public Page<Post> getPostsByUser(@PathVariable Long userId, Pageable pageable) {
        return postService.getPostsByUser(userId, pageable);
    }

    /**
     * Crea un nuevo post
     */
    @PostMapping("/user/{userId}/stream/{streamId}")
    public Post createPost(@Valid @RequestBody Post post,
                           @PathVariable Long userId,
                           @PathVariable Long streamId) {
        return postService.createPost(post, userId, streamId);
    }

    /**
     * Actualiza un post existente
     */
    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id, @Valid @RequestBody Post post) {
        return postService.updatePost(id, post);
    }

    /**
     * Elimina un post existente
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }
}