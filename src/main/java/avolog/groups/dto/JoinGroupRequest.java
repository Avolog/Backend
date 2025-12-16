package avolog.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record JoinGroupRequest(
        @Schema(description = "가입하려는 그룹 이름", example = "Avolog") @NotBlank String groupName,
        @Schema(description = "가입 비밀번호", example = "1234") @NotBlank String joinPassword
) {
}
