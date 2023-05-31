package com.rick.admin.auth.common;

import com.rick.admin.sys.user.entity.User;

/**
 * @author Rick
 * @createdAt 2021-04-08 14:37:00
 */
public class UserContextHolder {

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static void set(User user) {
        currentUser.set(user);
    }

    public static User get() {
        return currentUser.get();
    }

    public static void remove() {
        currentUser.remove();
    }

}
