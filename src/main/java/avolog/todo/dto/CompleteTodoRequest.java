package avolog.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record CompleteTodoRequest(
        @Schema(description = "Completion timestamp; default now if absent") OffsetDateTime completedAt
) {
}
