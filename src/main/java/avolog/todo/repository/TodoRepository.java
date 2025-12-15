package avolog.todo.repository;

import avolog.todo.domain.Todo;
import avolog.todo.domain.TodoStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, UUID> {

    List<Todo> findByCreatorUserId(UUID creatorUserId);

    List<Todo> findByGroupId(UUID groupId);

    List<Todo> findByRoutineIdAndPeriodKey(UUID routineId, String periodKey);

    long countByRoutineIdAndPeriodKeyAndStatus(UUID routineId, String periodKey, TodoStatus status);

    Optional<Todo> findByIdAndCreatorUserId(UUID id, UUID creatorUserId);
}
