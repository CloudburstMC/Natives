package com.nukkitx.natives.util;

import java.nio.ByteBuffer;

public class ByteBufferUtils {
    private static final long byteBufferArrayOffset = UnsafeUtils.objectFieldOffset(ByteBuffer.class, "hb");
    private static final long byteBufferOffsetOffset = UnsafeUtils.objectFieldOffset(ByteBuffer.class, "offset");

    public static byte[] getBufferArray(ByteBuffer byteBuffer) {
        return (byte[]) UnsafeUtils.getObject(byteBuffer, byteBufferArrayOffset);
    }

    public static int getBufferOffset(ByteBuffer byteBuffer) {
        return UnsafeUtils.getInt(byteBuffer, byteBufferOffsetOffset);
    }
}
