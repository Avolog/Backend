package avolog.todo.repository;

import avolog.todo.domain.TodoCompletion;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoCompletionRepository extends JpaRepository<TodoCompletion, UUID> {

    long countByRoutineIdAndPeriodKey(UUID routineId, String periodKey);
}
