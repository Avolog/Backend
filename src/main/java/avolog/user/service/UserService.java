package avolog.user.service;

import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import avolog.user.dto.GoogleLoginRequest ;
import avolog.user.dto.LoginResponse ;
import avolog.user.repository.UserRepository;

@Service
public class UserService {

    private final GoogleTokenClient tokenClient;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final GoogleIdTokenVerifier idTokenVerifier = new GoogleIdTokenVerifier();

    @Value("${app.google.client-id}")
    private String googleClientId;

    public UserService(GoogleTokenClient tokenClient, UserRepository userRepository, JwtService jwtService) {
        this.tokenClient = tokenClient;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse loginWithGoogle(GoogleLoginRequest req) {
        var token = tokenClient.exchangeCode(req.code(), req.codeVerifier());

        if (token == null || token.id_token() == null) {
            throw new IllegalArgumentException("Google token exchange failed (no id_token)");
        }

        JWTClaimsSet claims = idTokenVerifier.verify(token.id_token(), googleClientId);

        String sub = claims.getSubject();
        String email = (String) claims.getClaim("email");
        String name = (String) claims.getClaim("name");
        String picture = (String) claims.getClaim("picture");

        if (sub == null || email == null) {
            throw new IllegalArgumentException("Invalid Google claims");
        }

        UserEntity user = userRepository.findByGoogleSub(sub)
                .orElseGet(() -> {
                    UserEntity u = new UserEntity();
                    u.setGoogleSub(sub);
                    u.setEmail(email);
                    return u;
                });

        user.setName(name);
        user.setPictureUrl(picture);
        userRepository.save(user);

        String accessToken = jwtService.issue(user.getId(), user.getEmail());

        return new LoginResponse(accessToken, user.getId(), user.getEmail(), user.getName(), user.getPictureUrl());
    }
}