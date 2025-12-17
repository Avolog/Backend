package avolog.user.controller;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping(path = "/jwks.json", produces = "application/json")
    public Map<String, Object> jwks() {
        RSAPublicKey pub = loadPublicKey(publicKeyPem);

        JWK jwk = new RSAKey.Builder(pub)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(kid)
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
