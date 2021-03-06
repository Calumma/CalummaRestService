package ml.calumma.rest.repository;

import ml.calumma.model.entity.CalummaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<Entity extends CalummaEntity, T> extends JpaRepository<Entity, T>,
        JpaSpecificationExecutor<Entity> {
    Optional<Entity> findByIdAndIsDeletedFalse(Long id);
}
