package avolog.todo.dto;

import avolog.todo.domain.Todo;
import avolog.todo.domain.TodoStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TodoResponse(
        UUID id,
        String title,
        String description,
        TodoStatus status,
        LocalDate todoDate,
        String periodKey,
        UUID groupId,
        UUID categoryId,
        UUID routineId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getStatus(),
                todo.getTodoDate(),
                todo.getPeriodKey(),
                todo.getGroupId(),
                todo.getCategoryId(),
                todo.getRoutineId(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}
