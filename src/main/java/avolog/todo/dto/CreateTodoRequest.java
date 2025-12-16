package avolog.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateTodoRequest(
        @Schema(description = "Todo title") @NotBlank String title,
        @Schema(description = "Optional detailed description") String description,
        @Schema(description = "Target date for the todo") @NotNull LocalDate todoDate,
        @Schema(description = "Group id if group-scoped todo") UUID groupId,
        @Schema(description = "Category id for classification") UUID categoryId,
        @Schema(description = "Routine id if generated/linked to a routine") UUID routineId,
        @Schema(description = "Assignee user ids") List<UUID> assigneeIds
) {
}
