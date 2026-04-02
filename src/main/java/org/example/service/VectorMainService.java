package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.VectorMainMetadata;
import org.example.entity.VectorMain;
import org.example.repository.VectorMainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorMainService {

    private final VectorMainRepository repository;

    @Transactional
    public VectorMain uploadFile(String category, String fileName, byte[] fileData) {
        log.info("準備儲存檔案: {}, 大小: {} bytes", fileName, fileData.length);
        VectorMain entity = VectorMain.builder()
                .category(category)
                .fileName(fileName)
                .fileData(fileData)
                .fileSize((long) fileData.length)
                .build();
        return repository.save(entity);
    }

    // 取得所有檔案的「輕量」清單
    @Transactional(readOnly = true)
    public List<VectorMainMetadata> getAllFilesMetadata() {
        return repository.findAllMetadata();
    }

    // 當使用者真的需要下載檔案時，才精準撈取 BYTEA
    @Transactional(readOnly = true)
    public byte[] downloadFileData(UUID id) {
        log.info("正在讀取大型檔案內容，ID: {}", id);
        return repository.findFileDataById(id);
    }

    @Transactional
    public void deleteFile(UUID id) {
        log.info("刪除檔案，ID: {}", id);
        repository.deleteById(id);
    }
}