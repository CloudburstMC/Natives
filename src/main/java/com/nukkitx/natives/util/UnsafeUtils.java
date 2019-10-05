package com.nukkitx.natives.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class UnsafeUtils {
    private static final Unsafe UNSAFE;
    private static final Throwable UNSAFE_UNAVAILABILITY_CAUSE;

    static {
        final Object maybeUnsafe = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                    // We always want to try using Unsafe as the access still works on java9 as well and
                    // we need it for out native-transports and many optimizations.
                    unsafeField.setAccessible(true);
                    // the unsafe instance
                    return unsafeField.get(null);
                } catch (NoSuchFieldException | IllegalAccessException | NoClassDefFoundError | RuntimeException e) {
                    return e;
                }
            }
        });

        if (maybeUnsafe instanceof Throwable) {
            UNSAFE = null;
            UNSAFE_UNAVAILABILITY_CAUSE = (Throwable) maybeUnsafe;
        } else {
            UNSAFE = (Unsafe) maybeUnsafe;
            UNSAFE_UNAVAILABILITY_CAUSE = null;
        }
    }

    public static long objectFieldOffset(Class clazz, String field) {
        try {
            return UNSAFE.objectFieldOffset(clazz.getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getObject(Object object, long offset) {
        return UNSAFE.getObject(object, offset);
    }

    public static int getInt(Object object, long offset) {
        return UNSAFE.getInt(offset, offset);
    }
}
