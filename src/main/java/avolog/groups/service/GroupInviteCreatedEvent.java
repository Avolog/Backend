package avolog.groups.service;

import avolog.groups.model.GroupRole;

import java.time.Instant;
import java.util.UUID;

public record GroupInviteCreatedEvent(
        UUID inviteId,
        UUID groupId,
        UUID inviterUserId,
        UUID targetUserId,
        GroupRole role,
        Instant expiresAt
) {
}
