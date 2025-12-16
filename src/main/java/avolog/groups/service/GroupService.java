package avolog.groups.service;

import avolog.groups.dto.AcceptInviteResponse;
import avolog.groups.dto.CreateGroupRequest;
import avolog.groups.dto.CreateGroupResponse;
import avolog.groups.dto.CreateInviteRequest;
import avolog.groups.dto.InviteResponse;
import avolog.groups.dto.JoinGroupRequest;
import avolog.groups.dto.JoinGroupResponse;
import avolog.groups.dto.UpdateJoinPasswordRequest;
import avolog.groups.model.Group;
import avolog.groups.model.GroupInvite;
import avolog.groups.model.GroupMember;
import avolog.groups.model.GroupRole;
import avolog.groups.model.InviteStatus;
import avolog.groups.model.MemberStatus;
import avolog.groups.repository.GroupInviteRepository;
import avolog.groups.repository.GroupMemberRepository;
import avolog.groups.repository.GroupRepository;
import avolog.shared.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final GroupInviteRepository inviteRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CreateGroupResponse createGroup(CreateGroupRequest request, UUID userId) {
        if (groupRepository.existsByName(request.name())) {
            throw new ApiException(HttpStatus.CONFLICT, "group name already exists");
        }
        Instant now = Instant.now();
        Group group = Group.builder()
                .name(request.name())
                .description(request.description())
                .ownerUserId(userId)
                .joinPasswordHash(passwordEncoder.encode(request.joinPassword()))
                .joinPasswordUpdatedAt(now)
                .build();
        groupRepository.save(group);

        GroupMember owner = GroupMember.builder()
                .groupId(group.getId())
                .userId(userId)
                .role(GroupRole.OWNER)
                .status(MemberStatus.ACTIVE)
                .joinedAt(now)
                .build();
        memberRepository.save(owner);

        return new CreateGroupResponse(group.getId(), group.getName(), group.getDescription(),
                group.getOwnerUserId(), group.getCreatedAt());
    }

    @Transactional
    public JoinGroupResponse joinGroup(JoinGroupRequest request, UUID userId) {
        Group group = groupRepository.findByName(request.groupName())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "group not found"));

        if (!passwordEncoder.matches(request.joinPassword(), group.getJoinPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }

        GroupMember existing = memberRepository.findByGroupIdAndUserId(group.getId(), userId).orElse(null);
        if (existing != null && existing.getStatus() == MemberStatus.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "already a member");
        }

        Instant now = Instant.now();
        GroupMember member = existing != null
                ? existing
                : GroupMember.builder()
                .groupId(group.getId())
                .userId(userId)
                .build();
        member.setRole(GroupRole.MEMBER);
        member.setStatus(MemberStatus.ACTIVE);
        member.setJoinedAt(now);
        memberRepository.save(member);

        return new JoinGroupResponse(group.getId(), member.getRole(), member.getJoinedAt());
    }

    @Transactional
    public void updateJoinPassword(UUID groupId, UUID userId, UpdateJoinPasswordRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "group not found"));
        GroupMember member = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "membership required"));
        if (member.getRole() != GroupRole.OWNER && member.getRole() != GroupRole.MANAGER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "insufficient role");
        }
        group.setJoinPasswordHash(passwordEncoder.encode(request.newJoinPassword()));
        group.setJoinPasswordUpdatedAt(Instant.now());
        groupRepository.save(group);
    }

    @Transactional
    public InviteResponse createInvite(UUID groupId, UUID inviterUserId, CreateInviteRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "group not found"));
        GroupMember inviter = requireManagerOrOwner(groupId, inviterUserId);
        GroupRole role = request.role() == null ? GroupRole.MEMBER : request.role();
        if (memberRepository.existsByGroupIdAndUserIdAndStatus(groupId, request.targetUserId(), MemberStatus.ACTIVE)) {
            throw new ApiException(HttpStatus.CONFLICT, "target already a member");
        }
        inviteRepository.findByGroupIdAndTargetUserIdAndStatus(groupId, request.targetUserId(), InviteStatus.PENDING)
                .ifPresent(invite -> { throw new ApiException(HttpStatus.CONFLICT, "pending invite already exists"); });

        GroupInvite invite = GroupInvite.builder()
                .groupId(group.getId())
                .inviterUserId(inviter.getUserId())
                .targetUserId(request.targetUserId())
                .role(role)
                .status(InviteStatus.PENDING)
                .expiresAt(request.expiresAt())
                .build();
        inviteRepository.save(invite);

        return new InviteResponse(invite.getId(), invite.getGroupId(), group.getName(),
                invite.getInviterUserId(), invite.getTargetUserId(), invite.getRole(),
                invite.getStatus(), invite.getExpiresAt(), invite.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<InviteResponse> getReceivedInvites(UUID userId) {
        List<GroupInvite> invites = inviteRepository.findByTargetUserIdAndStatus(userId, InviteStatus.PENDING);
        return invites.stream()
                .map(invite -> {
                    String groupName = groupRepository.findById(invite.getGroupId())
                            .map(Group::getName)
                            .orElse("unknown");
                    return new InviteResponse(invite.getId(), invite.getGroupId(), groupName,
                            invite.getInviterUserId(), invite.getTargetUserId(), invite.getRole(),
                            invite.getStatus(), invite.getExpiresAt(), invite.getCreatedAt());
                }).toList();
    }

    @Transactional
    public AcceptInviteResponse acceptInvite(UUID inviteId, UUID userId) {
        GroupInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "invite not found"));
        if (!invite.getTargetUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "not your invite");
        }
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "invite already handled");
        }
        if (isExpired(invite)) {
            invite.setStatus(InviteStatus.EXPIRED);
            inviteRepository.save(invite);
            throw new ApiException(HttpStatus.CONFLICT, "invite expired");
        }
        GroupMember existingMember = memberRepository.findByGroupIdAndUserId(invite.getGroupId(), userId).orElse(null);
        if (existingMember != null && existingMember.getStatus() == MemberStatus.ACTIVE) {
            invite.setStatus(InviteStatus.REVOKED);
            inviteRepository.save(invite);
            throw new ApiException(HttpStatus.CONFLICT, "already a member");
        }
        invite.setStatus(InviteStatus.ACCEPTED);
        invite.setAcceptedAt(Instant.now());
        inviteRepository.save(invite);

        GroupMember member = existingMember != null ? existingMember : GroupMember.builder()
                .groupId(invite.getGroupId())
                .userId(userId)
                .build();
        member.setRole(invite.getRole());
        member.setStatus(MemberStatus.ACTIVE);
        member.setJoinedAt(Instant.now());
        memberRepository.save(member);

        return new AcceptInviteResponse(invite.getGroupId(), userId, member.getRole(), invite.getAcceptedAt());
    }

    @Transactional
    public void revokeInvite(UUID groupId, UUID inviteId, UUID requesterId) {
        GroupInvite invite = inviteRepository.findByGroupIdAndId(groupId, inviteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "invite not found"));
        requireManagerOrOwner(groupId, requesterId);
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "invite already handled");
        }
        invite.setStatus(InviteStatus.REVOKED);
        inviteRepository.save(invite);
    }

    private GroupMember requireManagerOrOwner(UUID groupId, UUID userId) {
        GroupMember member = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "membership required"));
        if (member.getRole() != GroupRole.OWNER && member.getRole() != GroupRole.MANAGER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "insufficient role");
        }
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "inactive membership");
        }
        return member;
    }

    private boolean isExpired(GroupInvite invite) {
        return invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(Instant.now());
    }
}
