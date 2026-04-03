package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.AuthUser;
import org.example.service.AuthUserService;
import org.springframework.stereotype.Component;

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
        AuthUser search = AuthUser.builder().name(name).build(); // 查詢條件

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
        log.info("[existsById] 使用者主鍵 {} 是否存在: {}", pkey, service.existsById(UUID.fromString(pkey)));

        // findById
        user = service.findById(UUID.fromString(pkey));
        if (user != null) {
            log.info("[findById] 使用者主鍵 {} 查詢結果: {} ({} 歲)", pkey, user.getName(), user.getAge());
        } else {
            log.info("[findById] 使用者主鍵 {} 查詢結果: 無此使用者", pkey);
        }

        // findOne
        user = service.findOne(search);
        log.info("[findOne] 使用者名稱 {} 的年齡: {}", search.getName(), user.getAge());

        // findAll (1)
        users = service.findAll();
        users.forEach(u -> log.info("[findAll (1)] 使用者: {} ({} 歲), 建立者: {}, 異動時間: {}",
                u.getName(), u.getAge(), u.getCreateUser(), u.getUpdateTime()));

        // findAll (2)
        users = service.findAll(search);
        users.forEach(u -> log.info("[findAll (2)] 使用者: {} ({} 歲), 建立者: {}, 異動時間: {}",
                u.getName(), u.getAge(), u.getCreateUser(), u.getUpdateTime()));

        // 修改
        age = 38;
        operator = "achi";
        if (user != null) {
            user.setAge(age);
            user.setUpdateUser(operator);
            user = service.save(user);
            log.info("使用者 {} 年齡({})已修改。", search.getName(), user.getAge());
        }

        // 查詢全部
        log.info("--- 目前使用者清單 (修改後) ---");
        users = service.findAll();
        users.forEach(u -> log.info("使用者: {} ({} 歲), 建立者: {}, 異動時間: {}",
                u.getName(), u.getAge(), u.getCreateUser(), u.getUpdateTime()));

        // 刪除
        service.deleteUser(user.getId());
        log.info("使用者 {} 已刪除。", name);

        // 查詢全部
        log.info("--- 目前使用者清單 (刪除後查詢) ---");
        users = service.findAll();
        log.info("當前使用者人數：{}", users.size());

        log.info("=== 執行完畢 ===");
    }
}