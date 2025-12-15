package avolog.user.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.*;
import com.nimbusds.jose.util.DefaultResourceRetriever;

import java.net.URL;
import java.util.List;

public class GoogleIdTokenVerifier {
    private final ConfigurableJWTProcessor<SecurityContext> processor;

    public GoogleIdTokenVerifier() {
        try {
            URL jwkUrl = new URL("https://www.googleapis.com/oauth2/v3/certs");
            var rr = new DefaultResourceRetriever(2000, 2000);
            var jwkSource = new RemoteJWKSet<SecurityContext>(jwkUrl, rr);

            processor = new DefaultJWTProcessor<>();
            JWSKeySelector<SecurityContext> keySelector =
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
            processor.setJWSKeySelector(keySelector);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to init Google JWK verifier", e);
        }
    }

    public JWTClaimsSet verify(String idToken, String expectedAudience) {
        try {
            JWTClaimsSet claims = processor.process(idToken, null);

            String iss = claims.getIssuer();
            if (!List.of("https://accounts.google.com", "accounts.google.com").contains(iss)) {
                throw new IllegalArgumentException("Invalid issuer: " + iss);
            }

            if (claims.getAudience() == null || !claims.getAudience().contains(expectedAudience)) {
                throw new IllegalArgumentException("Invalid audience");
            }

            return claims;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid id_token", e);
        }
    }
}
