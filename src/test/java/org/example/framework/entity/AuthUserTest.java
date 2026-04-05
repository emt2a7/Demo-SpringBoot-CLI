package org.example.framework.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import java.time.OffsetDateTime;

class AuthUserTest {
    @Test
    void testEntityFieldMapping() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        AuthUser user = AuthUser.builder()
                .id(id)
                .name("測試用戶")
                .age(30)
                .createTime(now)
                .createUser("admin")
                .updateTime(now)
                .updateUser("admin")
                .build();
        assertEquals(id, user.getId());
        assertEquals("測試用戶", user.getName());
        assertEquals(30, user.getAge());
        assertEquals(now, user.getCreateTime());
        assertEquals("admin", user.getCreateUser());
        assertEquals(now, user.getUpdateTime());
        assertEquals("admin", user.getUpdateUser());
    }
}

