package avolog.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class GoogleTokenClient {
    private final RestClient restClient = RestClient.create();

    @Value("${app.google.client-id}") private String clientId;
    @Value("${app.google.client-secret}") private String clientSecret;
    @Value("${app.google.redirect-uri}") private String redirectUri;

    public GoogleTokenResponse exchangeCode(String code, String codeVerifier) {
        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(Map.of(
                        "code", code,
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "redirect_uri", redirectUri,              // ✅ 프론트 callback과 동일해야 함
                        "grant_type", "authorization_code",
                        "code_verifier", codeVerifier             // ✅ PKCE
                ))
                .retrieve()
                .body(GoogleTokenResponse.class);
    }
    public record GoogleTokenResponse(
            String access_token,
            Integer expires_in,
            String scope,
            String token_type,
            String id_token
    ) {}

}
