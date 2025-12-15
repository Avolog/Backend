package avolog.todo.dto;

import java.time.OffsetDateTime;

public record CompleteTodoRequest(
        OffsetDateTime completedAt
) {
}
