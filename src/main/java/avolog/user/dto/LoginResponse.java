package avolog.user.dto;

public record LoginResponse(       String accessToken,
                                   Long userId,
                                   String email,
                                   String name,
                                   String pictureUrl) {
}
