package avolog.todo.service.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TodoEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        String producer,
        Map<String, Object> data
) {

    public static TodoEvent of(String eventType, Map<String, Object> data) {
        return new TodoEvent(UUID.randomUUID(), eventType, Instant.now(), "todo-service", data);
    }
}
