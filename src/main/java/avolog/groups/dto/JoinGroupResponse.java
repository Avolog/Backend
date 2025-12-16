package avolog.groups.dto;

import avolog.groups.model.GroupRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record JoinGroupResponse(
        @Schema(description = "그룹 ID") UUID groupId,
        @Schema(description = "부여된 역할") GroupRole role,
        @Schema(description = "가입 시각(UTC)") Instant joinedAt
) {
}
