package avolog.groups.service;

import java.time.Instant;
import java.util.UUID;

public record
GroupInviteRevokedEvent(
        UUID inviteId,
        UUID groupId,
        UUID revokedBy,
        Instant revokedAt
) {
}
