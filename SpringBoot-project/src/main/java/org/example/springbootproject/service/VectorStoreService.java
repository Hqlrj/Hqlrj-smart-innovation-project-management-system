package org.example.springbootproject.service;

import org.example.springbootproject.pojo.User;

import java.util.List;
import java.util.Map;

public interface VectorStoreService {

    void upsertChunks(String sourceType, Integer sourceId, Integer projectId, String college,
                      String title, List<String> texts);

    void deleteBySource(String sourceType, Integer sourceId);

    String search(String query, Integer projectId, User caller);
}
