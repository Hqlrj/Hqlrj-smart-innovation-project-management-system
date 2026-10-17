package org.example.springbootproject.service.impl;

import org.example.springbootproject.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 轻量哈希向量：不依赖额外 Embedding API，本地即可建库检索。
 * 需要更高语义效果时可换成 BGE-small-zh / OpenAI embeddings。
 */
@Service
public class HashEmbeddingService implements EmbeddingService {

    @Value("${ai.vector.dimension:384}")
    private int dimension;

    @Override
    public float[] embed(String text) {
        float[] vector = new float[dimension];
        if (text == null || text.isBlank()) {
            return vector;
        }
        String normalized = text.toLowerCase().replaceAll("\\s+", "");
        addToken(vector, normalized);
        for (int i = 0; i < normalized.length() - 1; i++) {
            addToken(vector, normalized.substring(i, i + 2));
        }
        for (int i = 0; i < normalized.length() - 2; i++) {
            addToken(vector, normalized.substring(i, i + 3));
        }
        normalize(vector);
        return vector;
    }

    @Override
    public String serialize(float[] vector) {
        StringBuilder sb = new StringBuilder(vector.length * 8);
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        return sb.toString();
    }

    @Override
    public float[] deserialize(String raw) {
        if (raw == null || raw.isBlank()) {
            return new float[dimension];
        }
        String[] parts = raw.split(",");
        float[] vector = new float[dimension];
        int n = Math.min(parts.length, dimension);
        for (int i = 0; i < n; i++) {
            try {
                vector[i] = Float.parseFloat(parts[i]);
            } catch (NumberFormatException ignored) {
            }
        }
        return vector;
    }

    @Override
    public double cosine(float[] a, float[] b) {
        if (a == null || b == null || a.length == 0) {
            return 0;
        }
        int n = Math.min(a.length, b.length);
        double dot = 0;
        double na = 0;
        double nb = 0;
        for (int i = 0; i < n; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) {
            return 0;
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private void addToken(float[] vector, String token) {
        int hash = token.hashCode();
        int index = Math.floorMod(hash, vector.length);
        vector[index] += 1.0f;
        int index2 = Math.floorMod(hash * 31, vector.length);
        vector[index2] += 0.5f;
    }

    private void normalize(float[] vector) {
        double sum = 0;
        for (float v : vector) {
            sum += v * v;
        }
        if (sum == 0) {
            return;
        }
        float norm = (float) Math.sqrt(sum);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= norm;
        }
    }
}
