package org.example.springbootproject.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.springbootproject.pojo.User;

/**
 * AI 查询工具：白名单执行，模型不能直接访问数据库。
 */
public interface AiToolExecutor {

    JsonArray toolDefinitions();

    String execute(String toolName, String argumentsJson, User caller);
}
