package avolog.groups.service;

import avolog.groups.model.GroupRole;

import java.time.Instant;
import java.util.UUID;

public record GroupInviteAcceptedEvent(
        UUID inviteId,
        UUID groupId,
        UUID userId,
        GroupRole role,
        Instant acceptedAt
) {
}
