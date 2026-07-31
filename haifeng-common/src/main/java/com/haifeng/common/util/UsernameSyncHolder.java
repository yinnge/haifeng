package com.haifeng.common.util;

public class UsernameSyncHolder {

    private static final ThreadLocal<Runnable> HOLDER = new ThreadLocal<>();

    public static void set(Runnable syncTask) {
        HOLDER.set(syncTask);
    }

    public static Runnable getAndClear() {
        Runnable task = HOLDER.get();
        HOLDER.remove();
        return task;
    }
}
