package avolog.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateTodoRequest(
        @Schema(description = "New title") String title,
        @Schema(description = "New description") String description,
        @Schema(description = "New target date") LocalDate todoDate,
        @Schema(description = "Category id") UUID categoryId,
        @Schema(description = "Routine id to relink") UUID routineId,
        @Schema(description = "Full replacement list of assignee user ids") List<UUID> assigneeIds
) {
}
