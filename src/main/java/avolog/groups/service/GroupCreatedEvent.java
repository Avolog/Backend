package avolog.groups.service;

import java.time.Instant;
import java.util.UUID;

public record GroupCreatedEvent(
        UUID groupId,
        String name,
        UUID ownerUserId,
        Instant createdAt
) {
}
