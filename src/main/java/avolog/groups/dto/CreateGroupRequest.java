package avolog.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateGroupRequest(
        @Schema(description = "그룹 이름 (고유)", example = "Avolog") @NotBlank String name,
        @Schema(description = "그룹 설명", example = "팀 작업 공간") String description,
        @Schema(description = "그룹 가입 비밀번호", example = "1234") @NotBlank String joinPassword
) {
}
