package avolog.todo.dto;

import avolog.todo.domain.Todo;
import avolog.todo.domain.TodoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TodoResponse(
        @Schema(description = "Todo id") UUID id,
        @Schema(description = "Title") String title,
        @Schema(description = "Description") String description,
        @Schema(description = "Status (PENDING|COMPLETED)") TodoStatus status,
        @Schema(description = "Target date") LocalDate todoDate,
        @Schema(description = "Computed period key") String periodKey,
        @Schema(description = "Group id if group todo") UUID groupId,
        @Schema(description = "Category id") UUID categoryId,
        @Schema(description = "Routine id") UUID routineId,
        @Schema(description = "Created at (UTC)") OffsetDateTime createdAt,
        @Schema(description = "Updated at (UTC)") OffsetDateTime updatedAt
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
