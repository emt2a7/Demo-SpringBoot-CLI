package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.framework.entity.AuthUser;
import org.example.framework.service.AuthUserService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUserCliController {

    private final AuthUserService service;

    public void runDemo() {
        log.info("=== 開始執行 AuthUser CRUD 展示 ===");

        AuthUser user = null;
        List<AuthUser> users = null;

        // 資料準備
        String pkey = "550e8400-e29b-41d4-a716-446655440000";
        String name = "張大明";
        int age = 26;
        String operator = "SystemAdmin";
        UUID id = UUID.fromString(pkey);
        AuthUser search = AuthUser.builder().name(name).build(); // 查詢條件
        List<AuthUser> entities = new ArrayList<>();

        // save (Create)
        user = AuthUser.builder()
                .name(name)
                .age(age)
                .createUser(operator)
                .updateUser(operator)
                .build();
        user = service.save(user);
        log.info("[save (Create)] 成功建立使用者，UUID: {}", user.getId());

        // exists
        log.info("[exists] 使用者名稱 {} 是否存在: {}", search.getName(), service.exists(search));

        // existsById
        log.info("[existsById] 使用者主鍵 {} 是否存在: {}", pkey, service.existsById(id));

        // findById
        user = service.findById(id).orElse(null);
        if (user != null) {
            log.info("[findById] 使用者主鍵 {} 查詢結果: {} ({} 歲)", pkey, user.getName(), user.getAge());
        } else {
            log.info("[findById] 使用者主鍵 {} 查詢結果: 無此使用者", pkey);
        }

        // findOne
        user = service.findOne(search).orElse(null);
        if (user != null) {
            log.info("[findOne] 使用者名稱 {} 的年齡: {}", search.getName(), user.getAge());
        } else {
            log.info("[findOne] 使用者名稱 {} 查詢結果: 無此使用者", search.getName());
        }

        // findAll (1)
        users = service.findAll();
        users.forEach(u -> log.info("[findAll (1)] 使用者: {} ({} 歲), 建立者: {}, 異動時間: {}",
                u.getName(), u.getAge(), u.getCreateUser(), u.getUpdateTime()));

        // save
        age = 38;
        operator = "John";
        if (user != null) {
            user.setAge(age);
            user.setUpdateUser(operator);
            user = service.save(user);
            log.info("[save] 使用者 {} 年齡({})已修改。", search.getName(), user.getAge());
        }

        // saveAndFlush
        age = 45;
        operator = "achi";
        if (user != null) {
            user.setAge(age);
            user.setUpdateUser(operator);
            user = service.saveAndFlush(user);
            log.info("[saveAndFlush] 使用者 {} 年齡({})已修改。", search.getName(), user.getAge());
        }

        // findAll (2)
        users = service.findAll(search);
        users.forEach(u -> log.info("[findAll (2)] 使用者: {} ({} 歲), 建立者: {}, 異動時間: {}",
                u.getName(), u.getAge(), u.getCreateUser(), u.getUpdateTime()));

        // saveAll
        service.saveAll(users);
        log.info("[saveAll] 儲存使用者資料，筆數：{}", users.size());

        // saveAll
        service.saveAllAndFlush(users);
        log.info("[saveAllAndFlush] 儲存使用者資料，筆數：{}", users.size());

        // deleteById
        if (user != null) {
            service.deleteById(user.getId());
            log.info("[deleteById] 使用者 {} 已刪除。", name);
        }

        // findAll
        log.info("--- 目前使用者清單 (刪除後查詢) ---");
        users = service.findAll();
        log.info("[findAll] 當前使用者人數：{}", users.size());

        log.info("=== 執行完畢 ===");
    }
}