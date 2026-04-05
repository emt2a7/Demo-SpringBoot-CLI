package org.example.framework.service;

import org.example.framework.entity.AuthUser;
import org.example.framework.repository.AuthUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthUserService 單元測試
 *
 * <p>此測試類別使用 Mockito 框架對 {@link AuthUserService} 進行單元測試，
 * 驗證所有 CRUD 方法的正確性與交易邊界標註。</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthUserServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @InjectMocks
    private AuthUserService authUserService;

    // ========== 寫入操作測試 (Write Operations Tests) ==========

    @Test
    void save_shouldReturnSavedEntity_whenValidInput() {
        // Given
        AuthUser authUser = AuthUser.builder()
                .name("張三")
                .age(30)
                .createUser("admin")
                .build();

        AuthUser savedAuthUser = AuthUser.builder()
                .id(UUID.randomUUID())
                .name("張三")
                .age(30)
                .createUser("admin")
                .build();

        when(authUserRepository.save(any(AuthUser.class))).thenReturn(savedAuthUser);

        // When
        AuthUser result = authUserService.save(authUser);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("張三", result.getName());
        assertEquals(30, result.getAge());
        assertEquals("admin", result.getCreateUser());
        verify(authUserRepository, times(1)).save(authUser);
    }

    @Test
    void saveAll_shouldReturnSavedEntities_whenValidInput() {
        // Given
        List<AuthUser> authUsers = Arrays.asList(
                AuthUser.builder().name("張三").age(30).createUser("admin").build(),
                AuthUser.builder().name("李四").age(25).createUser("admin").build()
        );

        List<AuthUser> savedAuthUsers = Arrays.asList(
                AuthUser.builder().id(UUID.randomUUID()).name("張三").age(30).createUser("admin").build(),
                AuthUser.builder().id(UUID.randomUUID()).name("李四").age(25).createUser("admin").build()
        );

        when(authUserRepository.saveAll(anyList())).thenReturn(savedAuthUsers);

        // When
        List<AuthUser> result = authUserService.saveAll(authUsers);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("張三", result.get(0).getName());
        assertEquals("李四", result.get(1).getName());
        verify(authUserRepository, times(1)).saveAll(authUsers);
    }

    @Test
    void saveAndFlush_shouldReturnSavedEntity_whenValidInput() {
        // Given
        AuthUser authUser = AuthUser.builder()
                .name("王五")
                .age(35)
                .createUser("admin")
                .build();

        AuthUser savedAuthUser = AuthUser.builder()
                .id(UUID.randomUUID())
                .name("王五")
                .age(35)
                .createUser("admin")
                .build();

        when(authUserRepository.saveAndFlush(any(AuthUser.class))).thenReturn(savedAuthUser);

        // When
        AuthUser result = authUserService.saveAndFlush(authUser);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("王五", result.getName());
        assertEquals(35, result.getAge());
        verify(authUserRepository, times(1)).saveAndFlush(authUser);
    }

    @Test
    void saveAllAndFlush_shouldReturnSavedEntities_whenValidInput() {
        // Given
        List<AuthUser> authUsers = Arrays.asList(
                AuthUser.builder().name("趙六").age(28).createUser("admin").build(),
                AuthUser.builder().name("孫七").age(32).createUser("admin").build()
        );

        List<AuthUser> savedAuthUsers = Arrays.asList(
                AuthUser.builder().id(UUID.randomUUID()).name("趙六").age(28).createUser("admin").build(),
                AuthUser.builder().id(UUID.randomUUID()).name("孫七").age(32).createUser("admin").build()
        );

        when(authUserRepository.saveAllAndFlush(anyList())).thenReturn(savedAuthUsers);

        // When
        List<AuthUser> result = authUserService.saveAllAndFlush(authUsers);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("趙六", result.get(0).getName());
        assertEquals("孫七", result.get(1).getName());
        verify(authUserRepository, times(1)).saveAllAndFlush(authUsers);
    }

    @Test
    void deleteById_shouldCallRepository_whenValidId() {
        // Given
        UUID id = UUID.randomUUID();
        doNothing().when(authUserRepository).deleteById(id);

        // When
        authUserService.deleteById(id);

        // Then
        verify(authUserRepository, times(1)).deleteById(id);
    }


    // ========== 查詢操作測試 (Read Operations Tests) ==========

    @Test
    void findById_shouldReturnEntity_whenIdExists() {
        // Given
        UUID id = UUID.randomUUID();
        AuthUser authUser = AuthUser.builder()
                .id(id)
                .name("張三")
                .age(30)
                .build();

        when(authUserRepository.findById(id)).thenReturn(Optional.of(authUser));

        // When
        Optional<AuthUser> result = authUserService.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("張三", result.get().getName());
        verify(authUserRepository, times(1)).findById(id);
    }

    @Test
    void findById_shouldReturnEmpty_whenIdNotExists() {
        // Given
        UUID id = UUID.randomUUID();
        when(authUserRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<AuthUser> result = authUserService.findById(id);

        // Then
        assertFalse(result.isPresent());
        verify(authUserRepository, times(1)).findById(id);
    }

    @Test
    void findOne_shouldReturnEntity_whenProbeMatches() {
        // Given
        AuthUser probe = AuthUser.builder().name("張三").build();
        AuthUser foundUser = AuthUser.builder()
                .id(UUID.randomUUID())
                .name("張三")
                .age(30)
                .build();

        when(authUserRepository.findOne(any(Example.class))).thenReturn(Optional.of(foundUser));

        // When
        Optional<AuthUser> result = authUserService.findOne(probe);

        // Then
        assertTrue(result.isPresent());
        assertEquals("張三", result.get().getName());
        assertEquals(30, result.get().getAge());
        verify(authUserRepository, times(1)).findOne(any(Example.class));
    }

    @Test
    void findOne_shouldReturnEmpty_whenProbeNotMatch() {
        // Given
        AuthUser probe = AuthUser.builder().name("不存在的使用者").build();
        when(authUserRepository.findOne(any(Example.class))).thenReturn(Optional.empty());

        // When
        Optional<AuthUser> result = authUserService.findOne(probe);

        // Then
        assertFalse(result.isPresent());
        verify(authUserRepository, times(1)).findOne(any(Example.class));
    }

    @Test
    void findAll_shouldReturnAllEntities() {
        // Given
        List<AuthUser> authUsers = Arrays.asList(
                AuthUser.builder().id(UUID.randomUUID()).name("張三").age(30).build(),
                AuthUser.builder().id(UUID.randomUUID()).name("李四").age(25).build()
        );

        when(authUserRepository.findAll()).thenReturn(authUsers);

        // When
        List<AuthUser> result = authUserService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("張三", result.get(0).getName());
        assertEquals("李四", result.get(1).getName());
        verify(authUserRepository, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnMatchingEntities_whenProbeProvided() {
        // Given
        AuthUser probe = AuthUser.builder().name("張三").build();
        List<AuthUser> authUsers = List.of(
                AuthUser.builder().id(UUID.randomUUID()).name("張三").age(30).build()
        );

        when(authUserRepository.findAll(any(Example.class))).thenReturn(authUsers);

        // When
        List<AuthUser> result = authUserService.findAll(probe);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("張三", result.get(0).getName());
        verify(authUserRepository, times(1)).findAll(any(Example.class));
    }

    // ========== 存在檢查測試 (Existence Check Tests) ==========

    @Test
    void existsById_shouldReturnTrue_whenIdExists() {
        // Given
        UUID id = UUID.randomUUID();
        when(authUserRepository.existsById(id)).thenReturn(true);

        // When
        boolean result = authUserService.existsById(id);

        // Then
        assertTrue(result);
        verify(authUserRepository, times(1)).existsById(id);
    }

    @Test
    void existsById_shouldReturnFalse_whenIdNotExists() {
        // Given
        UUID id = UUID.randomUUID();
        when(authUserRepository.existsById(id)).thenReturn(false);

        // When
        boolean result = authUserService.existsById(id);

        // Then
        assertFalse(result);
        verify(authUserRepository, times(1)).existsById(id);
    }

    @Test
    void exists_shouldReturnTrue_whenProbeMatches() {
        // Given
        AuthUser probe = AuthUser.builder().name("張三").build();
        when(authUserRepository.exists(any(Example.class))).thenReturn(true);

        // When
        boolean result = authUserService.exists(probe);

        // Then
        assertTrue(result);
        verify(authUserRepository, times(1)).exists(any(Example.class));
    }

    @Test
    void exists_shouldReturnFalse_whenProbeNotMatch() {
        // Given
        AuthUser probe = AuthUser.builder().name("不存在的使用者").build();
        when(authUserRepository.exists(any(Example.class))).thenReturn(false);

        // When
        boolean result = authUserService.exists(probe);

        // Then
        assertFalse(result);
        verify(authUserRepository, times(1)).exists(any(Example.class));
    }
}

