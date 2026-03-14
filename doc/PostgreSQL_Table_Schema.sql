-- ==========================================
-- RAG 向量資料庫的 PostgreSQL 結構設計
-- ==========================================
-- 確保喚醒兩個必備的擴充套件：向量引擎與 UUID 生成器
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 內文表 (Master Table): 儲存文件的核心元資料與完整原始內容
-- ==========================================
DROP TABLE IF EXISTS vector_main;
CREATE TABLE vector_main (
    id UUID     DEFAULT uuid_generate_v4() PRIMARY KEY, -- 使用 UUID 作為主鍵，確保全球唯一性
    category    VARCHAR(100),                           -- 文件類別，例如：報告、合約、研究論文等
    file_name   VARCHAR(255) UNIQUE NOT NULL,           -- 檔名 (包含副檔名)，不可重複
    file_data   BYTEA,                                  -- 用來儲存 Word/Excel/PDF 的原始二進位資料
    file_size   BIGINT,                                 -- 紀錄檔案大小 (Bytes)
    upload_time TIMESTAMP DEFAULT NOW()                 -- 上傳時間
);

-- ==========================================
-- 向量表 (Detail Table): Spring AI 預設的 Table
-- ==========================================
DROP TABLE IF EXISTS vector_store;
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID   DEFAULT uuid_generate_v4() PRIMARY KEY, -- 使用 UUID 作為主鍵，確保全球唯一性
    content   TEXT,                                   -- 切碎後的「原始文字」，例如：某一段落或句子
    metadata  JSONB,                                  -- 會在 metadata 裡面存入 vector_main 的 ID 或 file_name 作為軟性外鍵 (Soft FK)
    embedding vector(768)                             -- Gemini 模型的維度是 768，這裡必須精準對應！
);
-- 建立 HNSW 索引 (Hierarchical Navigable Small World)，這是目前向量搜尋中最快的演算法，能讓 AI 在百萬筆資料中瞬間找到關聯文字！
CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);

-- ==========================================
-- 向量表 (Detail Table): 人資 Table
-- ==========================================
DROP TABLE IF EXISTS hr_vector_store;
CREATE TABLE IF NOT EXISTS hr_vector_store (
    id UUID   DEFAULT uuid_generate_v4() PRIMARY KEY, -- 使用 UUID 作為主鍵，確保全球唯一性
    content   TEXT,                                   -- 切碎後的「原始文字」，例如：某一段落或句子
    metadata  JSONB,                                  -- 會在 metadata 裡面存入 vector_main 的 ID 或 file_name 作為軟性外鍵 (Soft FK)
    embedding vector(768)                             -- Gemini 模型的維度是 768，這裡必須精準對應！
);

-- 建立 HNSW 索引 (Hierarchical Navigable Small World)，這是目前向量搜尋中最快的演算法，能讓 AI 在百萬筆資料中瞬間找到關聯文字！
CREATE INDEX ON hr_vector_store USING HNSW (embedding vector_cosine_ops);

-- ==========================================
-- 向量表 (Detail Table): IT Table
-- ==========================================
DROP TABLE IF EXISTS it_vector_store;
CREATE TABLE IF NOT EXISTS it_vector_store (
    id UUID   DEFAULT uuid_generate_v4() PRIMARY KEY, -- 使用 UUID 作為主鍵，確保全球唯一性
    content   TEXT,                                   -- 切碎後的「原始文字」，例如：某一段落或句子
    metadata  JSONB,                                  -- 會在 metadata 裡面存入 vector_main 的 ID 或 file_name 作為軟性外鍵 (Soft FK)
    embedding vector(768)                             -- Gemini 模型的維度是 768，這裡必須精準對應！
);
-- 建立 HNSW 索引 (Hierarchical Navigable Small World)，這是目前向量搜尋中最快的演算法，能讓 AI 在百萬筆資料中瞬間找到關聯文字！
CREATE INDEX ON it_vector_store USING HNSW (embedding vector_cosine_ops);

-- ==========================================
-- 向量表 (Detail Table): Baby Table
-- ==========================================
DROP TABLE IF EXISTS baby_vector_store;
CREATE TABLE IF NOT EXISTS baby_vector_store (
    id UUID   DEFAULT uuid_generate_v4() PRIMARY KEY, -- 使用 UUID 作為主鍵，確保全球唯一性
    content   TEXT,                                   -- 切碎後的「原始文字」，例如：某一段落或句子
    metadata  JSONB,                                  -- 會在 metadata 裡面存入 vector_main 的 ID 或 file_name 作為軟性外鍵 (Soft FK)
    embedding vector(768)                             -- Gemini 模型的維度是 768，這裡必須精準對應！
);
-- 建立 HNSW 索引 (Hierarchical Navigable Small World)，這是目前向量搜尋中最快的演算法，能讓 AI 在百萬筆資料中瞬間找到關聯文字！
CREATE INDEX ON baby_vector_store USING HNSW (embedding vector_cosine_ops);



