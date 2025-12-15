package avolog.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import avolog.user.service.UserEntity;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>{
    Optional<UserEntity> findByGoogleSub(String googleSub);
}
