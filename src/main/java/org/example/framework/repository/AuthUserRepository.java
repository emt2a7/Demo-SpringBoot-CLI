package org.example.framework.repository;

import org.example.framework.entity.AuthUser;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthUserRepository extends BaseRepository<AuthUser, UUID> {
}

