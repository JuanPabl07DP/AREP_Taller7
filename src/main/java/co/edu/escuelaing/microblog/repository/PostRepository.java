package co.edu.escuelaing.microblog.repository;

import co.edu.escuelaing.microblog.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByStream(Stream stream, Pageable pageable);
    Page<Post> findByUser(User user, Pageable pageable);
}