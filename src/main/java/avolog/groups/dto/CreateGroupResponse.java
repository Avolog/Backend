package avolog.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record CreateGroupResponse(
        @Schema(description = "그룹 ID") UUID groupId,
        @Schema(description = "그룹 이름") String name,
        @Schema(description = "그룹 설명") String description,
        @Schema(description = "그룹 소유자 사용자 ID") UUID ownerUserId,
        @Schema(description = "생성 시각(UTC)") Instant createdAt
) {
}
