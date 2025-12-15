package avolog.todo.repository;

import avolog.todo.domain.Routine;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineRepository extends JpaRepository<Routine, UUID> {
}
