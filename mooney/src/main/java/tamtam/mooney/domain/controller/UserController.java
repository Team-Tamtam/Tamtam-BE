package tamtam.mooney.domain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.service.UserService;

@Tag(name = "User")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "유저 닉네임 조회")
    @GetMapping("/nickname")
    public ResponseEntity<?> getUserInfo() {
        return ResponseEntity.ok().body(userService.getUserNickname());
    }
}
