package avolog.user.controller;

import com.nimbusds.jose.jwk.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/users/.well-known")
public class JwksController {

    @Value("${app.jwt.public-key-pem}") private String publicKeyPem;
    @Value("${app.jwt.kid}") private String kid;

    @GetMapping("/jwks.json")
    public Map<String, Object> jwks() {
        RSAPublicKey pub = loadPublicKey(publicKeyPem);

        JWK jwk = new RSAKey.Builder(pub)
                .keyID(kid)
                .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();

        return new JWKSet(jwk).toJSONObject();
    }

    private RSAPublicKey loadPublicKey(String pem) {
        try {
            String normalized = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(normalized);
            var spec = new X509EncodedKeySpec(der);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key", e);
        }
    }
}