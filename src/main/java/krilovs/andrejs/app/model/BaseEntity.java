package krilovs.andrejs.app.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

/**
 * Common optimistic-lock version. The id lives in each entity, because every entity
 * has its own database sequence (see the db/changelog migrations).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @Version
    private Long version;
}
