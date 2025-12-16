package avolog.groups.dto;

import avolog.groups.model.GroupRole;
import avolog.groups.model.InviteStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record InviteResponse(
        @Schema(description = "초대 ID") UUID inviteId,
        @Schema(description = "그룹 ID") UUID groupId,
        @Schema(description = "그룹 이름") String groupName,
        @Schema(description = "초대한 사용자 ID") UUID inviterUserId,
        @Schema(description = "초대 대상 사용자 ID") UUID targetUserId,
        @Schema(description = "부여할 역할") GroupRole role,
        @Schema(description = "초대 상태") InviteStatus status,
        @Schema(description = "만료 시각(UTC)") Instant expiresAt,
        @Schema(description = "생성 시각(UTC)") Instant createdAt
) {
}
