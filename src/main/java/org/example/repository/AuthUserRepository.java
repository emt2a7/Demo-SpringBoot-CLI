package org.example.repository;

import org.example.entity.AuthUser;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AuthUserRepository extends BaseRepository<AuthUser, UUID> {
    // 若未來需要根據姓名搜尋，可在此擴充：Optional<AuthUser> findByName(String name);
}