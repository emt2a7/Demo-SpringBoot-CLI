package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.AuthUser;
import org.example.repository.AuthUserRepository;
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

    private final AuthUserRepository repository;

    @Transactional(readOnly = true)
    public boolean exists(AuthUser entity) {
        return repository.exists(Example.of(entity));
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Transactional(readOnly = true)
    public AuthUser findById(UUID id) {
        Optional<AuthUser> result = repository.findById(id);
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public AuthUser findOne(AuthUser entity) {
        Optional<AuthUser> result = repository.findOne(Example.of(entity));

        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<AuthUser> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AuthUser> findAll(AuthUser entity) {
        return repository.findAll(Example.of(entity));
    }

    @Transactional
    public AuthUser save(AuthUser authUser) {
        return repository.save(authUser);
    }

    @Transactional
    public AuthUser saveAndFlush(AuthUser authUser) {
        return repository.saveAndFlush(authUser);
    }

    @Transactional
    public List<AuthUser> saveAll(List<AuthUser> entities) {
        return repository.saveAll(entities);
    }

    @Transactional
    public List<AuthUser> saveAllAndFlush(List<AuthUser> entities) {
        return repository.saveAllAndFlush(entities);
    }

    @Transactional
    public void deleteUser(UUID id) {
        repository.deleteById(id);
    }
}