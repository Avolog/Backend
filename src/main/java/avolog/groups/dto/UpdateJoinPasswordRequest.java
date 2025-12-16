package avolog.groups.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateJoinPasswordRequest(
        @Schema(description = "새 가입 비밀번호", example = "abcd") @NotBlank String newJoinPassword
) {
}
