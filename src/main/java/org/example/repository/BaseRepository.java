package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * 所有 Repository 的共用基底介面。
 * 加上 @NoRepositoryBean 避免 Spring 嘗試實例化它。
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    // 未來若有所有資料表共用的自訂方法 (例如軟刪除 common method)，可定義於此
}