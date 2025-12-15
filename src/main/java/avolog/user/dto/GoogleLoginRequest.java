package avolog.user.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(@NotBlank(message = "code값이 필요합니다.") String code,
                                 @NotBlank(message = "codeVerifier가 필요합니다.") String codeVerifier) {

}
