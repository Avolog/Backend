package avolog.groups.dto;

import avolog.groups.model.GroupRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateInviteRequest(
        @Schema(description = "초대 대상 사용자 ID", example = "c1f62e23-8b2c-4f39-9a36-3c4efac8f0e3") @NotNull UUID targetUserId,
        @Schema(description = "부여할 역할 (기본 MEMBER)", example = "MEMBER") GroupRole role,
        @Schema(description = "초대 만료 시각(UTC), 없으면 제한 없음", example = "2025-12-20T00:00:00Z") Instant expiresAt
) {
}
