package org.example.repository;

import org.example.dto.VectorMainMetadata;
import org.example.entity.VectorMain;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VectorMainRepository extends BaseRepository<VectorMain, UUID> {

    // 🌟 核心設計 1：只撈取 Metadata (完全避開 file_data)
    @Query("SELECT new org.example.dto.VectorMainMetadata(v.id, v.category, v.fileName, v.fileSize, v.uploadTime) FROM VectorMain v")
    List<VectorMainMetadata> findAllMetadata();

    // 🌟 核心設計 2：只透過 ID 撈取巨大的 BYTEA 檔案內容
    @Query("SELECT v.fileData FROM VectorMain v WHERE v.id = :id")
    byte[] findFileDataById(@Param("id") UUID id);

    // 🌟 核心設計 3：單筆撈取 Metadata (不抓 BYTEA，適合用在 Update 前的檢查)
    @Query("SELECT new org.example.dto.VectorMainMetadata(v.id, v.category, v.fileName, v.fileSize, v.uploadTime) FROM VectorMain v WHERE v.id = :id")
    Optional<VectorMainMetadata> findMetadataById(@Param("id") UUID id);

    // 依檔名尋找 (確保檔名唯一性的查詢)
    Optional<VectorMain> findByFileName(String fileName);
}