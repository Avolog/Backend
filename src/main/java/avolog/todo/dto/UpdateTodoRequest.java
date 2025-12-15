package avolog.todo.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateTodoRequest(
        String title,
        String description,
        LocalDate todoDate,
        UUID categoryId,
        UUID routineId,
        List<UUID> assigneeIds
) {
}
