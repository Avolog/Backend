package avolog.groups.controller;

import avolog.groups.dto.CreateGroupRequest;
import avolog.groups.dto.CreateGroupResponse;
import avolog.groups.dto.JoinGroupRequest;
import avolog.groups.dto.JoinGroupResponse;
import avolog.groups.dto.UpdateJoinPasswordRequest;
import avolog.groups.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "그룹 생성/가입 관련 API")
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "그룹 생성", description = "이름, 설명, 가입비밀번호로 그룹을 생성하고 요청자가 OWNER로 등록됩니다.")
    @PostMapping
    public ResponseEntity<CreateGroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @Parameter(description = "요청 사용자 ID (JWT에서 추출 예정)") @RequestHeader("X-User-Id") UUID userId
    ) {
        CreateGroupResponse response = groupService.createGroup(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "그룹 가입(이름+비밀번호)", description = "groupName과 joinPassword로 그룹을 찾아 멤버로 가입합니다.")
    @PostMapping("/join")
    public ResponseEntity<JoinGroupResponse> joinGroup(
            @Valid @RequestBody JoinGroupRequest request,
            @Parameter(description = "요청 사용자 ID (JWT에서 추출 예정)") @RequestHeader("X-User-Id") UUID userId
    ) {
        JoinGroupResponse response = groupService.joinGroup(request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "그룹 가입비밀번호 변경", description = "OWNER 또는 MANAGER 권한일 때 비밀번호를 최신화합니다.")
    @PatchMapping("/{groupId}/join-password")
    public ResponseEntity<Void> updateJoinPassword(
            @Parameter(description = "그룹 ID") @PathVariable UUID groupId,
            @Valid @RequestBody UpdateJoinPasswordRequest request,
            @Parameter(description = "요청 사용자 ID (JWT에서 추출 예정)") @RequestHeader("X-User-Id") UUID userId
    ) {
        groupService.updateJoinPassword(groupId, userId, request);
        return ResponseEntity.noContent().build();
    }
}
