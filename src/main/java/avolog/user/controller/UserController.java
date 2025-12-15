package avolog.user.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import avolog.user.dto.GoogleLoginRequest;
import avolog.user.dto.LoginResponse;
import avolog.user.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping("/auth/google")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody @Valid GoogleLoginRequest req) {
        return ResponseEntity.ok(userService.loginWithGoogle(req));
    }

    @GetMapping("/health")
    public String health() { return "ok"; }
}