package com.nukkitx.natives.sha256;

import com.nukkitx.natives.util.ByteBufferUtils;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public class NativeSha256 implements Sha256 {
    public static final Supplier<Sha256> SUPPLIER = NativeSha256::new;

    private volatile long ctx;

    private NativeSha256() {
        this.ctx = this.init();
    }

    public void update(byte[] bytes, int offset, int length) {
        this.ensureOpen();
        this.update(this.ctx, bytes, offset, length);
    }

    public void update(ByteBuffer buffer) {
        this.ensureOpen();
        if (buffer.isDirect()) {
            this.update(this.ctx, ((DirectBuffer) buffer).address() + buffer.arrayOffset(), buffer.remaining());
        } else {
            byte[] array = ByteBufferUtils.getBufferArray(buffer);
            int offset = ByteBufferUtils.getBufferOffset(buffer);
            this.update(this.ctx, array, offset, buffer.remaining());
        }
    }

    public byte[] digest() {
        return this.digest(this.ctx);
    }

    @Override
    public void reset() {
        this.ensureOpen();
        this.reset(this.ctx);
    }

    @Override
    public synchronized void free() {
        if (this.ctx != 0) {
            this.free(this.ctx);
            this.ctx = 0;
        }
    }

    private void ensureOpen() {
        if (this.ctx == 0) throw new IllegalStateException("Native resource has already been freed");
    }

    private native long init();

    private native void free(long ctx);

    private native void reset(long ctx);

    private native void update(long ctx, long in, int length);

    private native void update(long ctx, byte[] bytes, int offset, int length);

    private native byte[] digest(long ctx);
}
