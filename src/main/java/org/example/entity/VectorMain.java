package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vector_main")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VectorMain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    private String category;

    @Column(name = "file_name", length = 255, unique = true, nullable = false)
    private String fileName;

    // BYTEA 映射為 byte[]。我們不依賴 FetchType.LAZY，而是透過 DTO 確保安全
    @Column(name = "file_data")
    private byte[] fileData;

    @Column(name = "file_size")
    private Long fileSize;

    // 讓 JPA 自動在 Insert 時填入當下時間，對應 DB 的 DEFAULT NOW()
    @CreationTimestamp
    @Column(name = "upload_time", updatable = false)
    private LocalDateTime uploadTime;
}