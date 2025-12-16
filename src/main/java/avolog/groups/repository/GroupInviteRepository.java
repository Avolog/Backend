package avolog.groups.repository;

import avolog.groups.model.GroupInvite;
import avolog.groups.model.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, UUID> {
    List<GroupInvite> findByTargetUserIdAndStatus(UUID targetUserId, InviteStatus status);
    Optional<GroupInvite> findByGroupIdAndId(UUID groupId, UUID id);
    Optional<GroupInvite> findByGroupIdAndTargetUserIdAndStatus(UUID groupId, UUID targetUserId, InviteStatus status);
}
