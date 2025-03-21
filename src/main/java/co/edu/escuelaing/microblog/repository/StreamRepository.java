package co.edu.escuelaing.microblog.repository;

import co.edu.escuelaing.microblog.model.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StreamRepository extends JpaRepository<Stream, Long> {
    Optional<Stream> findByName(String name);
    Boolean existsByName(String name);
}