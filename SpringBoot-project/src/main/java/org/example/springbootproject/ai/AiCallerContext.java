package org.example.springbootproject.ai;

import org.example.springbootproject.pojo.User;

public final class AiCallerContext {

    private static final ThreadLocal<User> HOLDER = new ThreadLocal<>();

    private AiCallerContext() {
    }

    public static void set(User user) {
        HOLDER.set(user);
    }

    public static User get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
