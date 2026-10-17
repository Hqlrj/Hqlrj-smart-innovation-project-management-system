package org.example.springbootproject.ai;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 会话记忆；连不上时回退到进程内存，保证系统仍能对话。
 */
@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    private static final Logger log = LoggerFactory.getLogger(RedisChatMemoryStore.class);

    @Value("${ai.redis.host:localhost}")
    private String host;

    @Value("${ai.redis.port:6379}")
    private int port;

    @Value("${ai.redis.ttl-seconds:86400}")
    private long ttlSeconds;

    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> commands;
    private final Map<Object, String> memoryFallback = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            RedisURI uri = RedisURI.builder()
                    .withHost(host)
                    .withPort(port)
                    .withTimeout(Duration.ofSeconds(2))
                    .build();
            client = RedisClient.create(uri);
            connection = client.connect();
            commands = connection.sync();
            commands.ping();
            log.info("Redis 会话记忆已连接 {}:{}", host, port);
        } catch (Exception e) {
            log.warn("Redis 不可用，会话记忆改用内存: {}", e.getMessage());
            closeQuietly();
        }
    }

    @PreDestroy
    public void destroy() {
        closeQuietly();
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = read(key(memoryId));
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(ChatMessageDeserializer.messagesFromJson(json));
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        write(key(memoryId), ChatMessageSerializer.messagesToJson(messages));
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String k = key(memoryId);
        if (commands != null) {
            try {
                commands.del(k);
                return;
            } catch (Exception e) {
                log.debug("Redis 删除失败: {}", e.getMessage());
            }
        }
        memoryFallback.remove(k);
    }

    public boolean usingRedis() {
        return commands != null;
    }

    private String key(Object memoryId) {
        return "ai:memory:" + memoryId;
    }

    private String read(String key) {
        if (commands != null) {
            try {
                return commands.get(key);
            } catch (Exception e) {
                log.debug("Redis 读取失败: {}", e.getMessage());
            }
        }
        return memoryFallback.get(key);
    }

    private void write(String key, String value) {
        if (commands != null) {
            try {
                commands.setex(key, ttlSeconds, value);
                return;
            } catch (Exception e) {
                log.debug("Redis 写入失败: {}", e.getMessage());
            }
        }
        memoryFallback.put(key, value);
    }

    private void closeQuietly() {
        commands = null;
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {
            }
            connection = null;
        }
        if (client != null) {
            try {
                client.shutdown();
            } catch (Exception ignored) {
            }
            client = null;
        }
    }
}
