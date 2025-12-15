package avolog.todo.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RequestHeaderCurrentUserProvider implements CurrentUserProvider {

    private static final String USER_HEADER = "X-User-Id";
    private final HttpServletRequest request;

    public RequestHeaderCurrentUserProvider(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public UUID getCurrentUserId() {
        String raw = request.getHeader(USER_HEADER);
        return Optional.ofNullable(raw)
                .map(this::toUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header"));
    }

    private UUID toUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid X-User-Id header");
        }
    }
}
