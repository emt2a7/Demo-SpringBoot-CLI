package org.example.framework.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.framework.entity.AuthUser;
import org.example.framework.repository.AuthUserRepository;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserService {

    private final AuthUserRepository authUserRepository;

    @Transactional(readOnly = true)
    public boolean exists(AuthUser entity) {
        return authUserRepository.exists(Example.of(entity));
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return authUserRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AuthUser> findById(UUID id) {
        return authUserRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AuthUser> findOne(AuthUser entity) {
        return authUserRepository.findOne(Example.of(entity));
    }

    @Transactional(readOnly = true)
    public List<AuthUser> findAll() {
        return authUserRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AuthUser> findAll(AuthUser entity) {
        return authUserRepository.findAll(Example.of(entity));
    }

    @Transactional
    public AuthUser save(AuthUser entity) {
        AuthUser saved = authUserRepository.save(entity);
        log.info("AuthUser saved successfully, id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public AuthUser saveAndFlush(AuthUser entity) {
        AuthUser saved = authUserRepository.saveAndFlush(entity);
        log.info("AuthUser saved and flushed successfully, id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public List<AuthUser> saveAll(List<AuthUser> entities) {
        List<AuthUser> saved = authUserRepository.saveAll(entities);
        log.info("AuthUser batch saved successfully, count={}", saved.size());
        return saved;
    }

    @Transactional
    public List<AuthUser> saveAllAndFlush(List<AuthUser> entities) {
        List<AuthUser> saved = authUserRepository.saveAllAndFlush(entities);
        log.info("AuthUser batch saved and flushed successfully, count={}", saved.size());
        return saved;
    }

    @Transactional
    public void deleteById(UUID id) {
        authUserRepository.deleteById(id);
        log.info("AuthUser deleted successfully, id={}", id);
    }
}

