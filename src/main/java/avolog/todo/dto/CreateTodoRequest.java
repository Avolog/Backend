package avolog.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateTodoRequest(
        @NotBlank String title,
        String description,
        @NotNull LocalDate todoDate,
        UUID groupId,
        UUID categoryId,
        UUID routineId,
        List<UUID> assigneeIds
) {
}
