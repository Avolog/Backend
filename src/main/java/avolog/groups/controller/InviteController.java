package avolog.groups.controller;

import avolog.groups.dto.AcceptInviteResponse;
import avolog.groups.dto.CreateInviteRequest;
import avolog.groups.dto.InviteResponse;
import avolog.groups.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "Invites", description = "그룹 초대 발급/조회/수락/취소 API")
public class InviteController {

    private final GroupService groupService;

    @Operation(summary = "그룹 초대 생성", description = "MANAGER 이상이 초대장을 생성합니다.")
    @PostMapping("/groups/{groupId}/invites")
    public ResponseEntity<InviteResponse> createInvite(
            @Parameter(description = "그룹 ID") @PathVariable UUID groupId,
            @Valid @RequestBody CreateInviteRequest request,
            @Parameter(description = "요청 사용자 ID (JWT에서 추출 예정)") @RequestHeader("X-User-Id") UUID userId
    ) {
        InviteResponse response = groupService.createInvite(groupId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "받은 초대 목록 조회", description = "내게 온 PENDING 초대장을 조회합니다.")
    @GetMapping("/invites/received")
    public ResponseEntity<List<InviteResponse>> getReceived(
            @Parameter(description = "요청 사용자 ID (JWT에서 추출 예정)") @RequestHeader("X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(groupService.getReceivedInvites(userId));
    }

    @Operation(summary = "초대 수락", description = "초대 대상자가 초대를 수락하고 멤버로 등록됩니다.")
    @PostMapping("/invites/{inviteId}/accept")
    public ResponseEntity<AcceptInviteResponse> acceptInvite(
            @Parameter(description = "초대 ID") @PathVariable UUID inviteId,
            @Parameter(description = "요청 사용자 ID (JWT에서 추출 예정)") @RequestHeader("X-User-Id") UUID userId
    ) {
        AcceptInviteResponse response = groupService.acceptInvite(inviteId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "초대 취소", description = "MANAGER 이상이 PENDING 초대를 취소합니다.")
    @PostMapping("/groups/{groupId}/invites/{inviteId}/revoke")
    public ResponseEntity<Void> revokeInvite(
            @Parameter(description = "그룹 ID") @PathVariable UUID groupId,
            @Parameter(description = "초대 ID") @PathVariable UUID inviteId,
            @Parameter(description = "요청 사용자 ID (JWT에서 추출 예정)") @RequestHeader("X-User-Id") UUID userId
    ) {
        groupService.revokeInvite(groupId, inviteId, userId);
        return ResponseEntity.noContent().build();
    }
}
