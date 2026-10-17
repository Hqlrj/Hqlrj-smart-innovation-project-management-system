package org.example.springbootproject.service;

/**
 * 文本向量化。当前用稳定可离线的哈希向量，接口可替换为 BGE / OpenAI Embedding。
 */
public interface EmbeddingService {

    float[] embed(String text);

    String serialize(float[] vector);

    float[] deserialize(String raw);

    double cosine(float[] a, float[] b);
}
