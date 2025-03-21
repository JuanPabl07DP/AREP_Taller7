package co.edu.escuelaing.microblog.controller;

import co.edu.escuelaing.microblog.model.Post;
import co.edu.escuelaing.microblog.model.Stream;
import co.edu.escuelaing.microblog.model.User;
import co.edu.escuelaing.microblog.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private Post testPost;
    private User testUser;
    private Stream testStream;
    private Page<Post> postPage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testStream = new Stream();
        testStream.setId(1L);
        testStream.setName("teststream");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setContent("Test post content");
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUser(testUser);
        testPost.setStream(testStream);

        List<Post> posts = new ArrayList<>();
        posts.add(testPost);
        postPage = new PageImpl<>(posts);
    }

    @Test
    void getAllPosts_ShouldReturnPageOfPosts() {
        // Arrange
        when(postService.getAllPosts(any(Pageable.class))).thenReturn(postPage);

        // Act
        Page<Post> result = postController.getAllPosts(Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(postService, times(1)).getAllPosts(any(Pageable.class));
    }

    @Test
    void getPostById_ShouldReturnPost() {
        // Arrange
        when(postService.getPostById(1L)).thenReturn(testPost);

        // Act
        Post result = postController.getPostById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test post content", result.getContent());
        verify(postService, times(1)).getPostById(1L);
    }

    @Test
    void getPostsByStream_ShouldReturnPageOfPosts() {
        // Arrange
        when(postService.getPostsByStream(eq(1L), any(Pageable.class))).thenReturn(postPage);

        // Act
        Page<Post> result = postController.getPostsByStream(1L, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(postService, times(1)).getPostsByStream(eq(1L), any(Pageable.class));
    }

    @Test
    void getPostsByUser_ShouldReturnPageOfPosts() {
        // Arrange
        when(postService.getPostsByUser(eq(1L), any(Pageable.class))).thenReturn(postPage);

        // Act
        Page<Post> result = postController.getPostsByUser(1L, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(postService, times(1)).getPostsByUser(eq(1L), any(Pageable.class));
    }

    @Test
    void createPost_ShouldReturnCreatedPost() {
        // Arrange
        when(postService.createPost(any(Post.class), eq(1L), eq(1L))).thenReturn(testPost);

        // Act
        Post result = postController.createPost(new Post(), 1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test post content", result.getContent());
        verify(postService, times(1)).createPost(any(Post.class), eq(1L), eq(1L));
    }

    @Test
    void updatePost_ShouldReturnUpdatedPost() {
        // Arrange
        when(postService.updatePost(eq(1L), any(Post.class))).thenReturn(testPost);

        // Act
        Post result = postController.updatePost(1L, new Post());

        // Assert
        assertNotNull(result);
        assertEquals("Test post content", result.getContent());
        verify(postService, times(1)).updatePost(eq(1L), any(Post.class));
    }

    @Test
    void deletePost_ShouldReturnOkResponse() {
        // Arrange
        doNothing().when(postService).deletePost(1L);

        // Act
        ResponseEntity<?> response = postController.deletePost(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(postService, times(1)).deletePost(1L);
    }
}