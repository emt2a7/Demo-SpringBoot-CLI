package org.example.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 輕量級 DTO，刻意排除 fileData (BYTEA)，避免列表查詢時發生 OOM (Out Of Memory)
 */
public record VectorMainMetadata(
        UUID id,
        String category,
        String fileName,
        Long fileSize,
        LocalDateTime uploadTime
) {}