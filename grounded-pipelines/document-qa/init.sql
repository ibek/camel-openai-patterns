-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Chunks table with vector embeddings
CREATE TABLE IF NOT EXISTS chunks (
    id SERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    source VARCHAR(255),
    chunk_index INTEGER,
    embedding vector(768),  -- nomic-embed-text dimension
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for fast similarity search (run after loading data)
-- CREATE INDEX chunks_embedding_idx ON chunks USING hnsw (embedding vector_cosine_ops);
