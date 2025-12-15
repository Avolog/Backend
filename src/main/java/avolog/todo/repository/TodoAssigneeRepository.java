package avolog.todo.repository;

import avolog.todo.domain.TodoAssignee;
import avolog.todo.domain.TodoAssigneeId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoAssigneeRepository extends JpaRepository<TodoAssignee, TodoAssigneeId> {

    List<TodoAssignee> findByIdTodoId(UUID todoId);

    boolean existsByIdTodoIdAndIdUserId(UUID todoId, UUID userId);

    void deleteByIdTodoId(UUID todoId);
}
