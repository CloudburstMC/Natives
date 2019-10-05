package com.nukkitx.natives.aes;

import com.nukkitx.natives.util.ByteBufferUtils;
import sun.nio.ch.DirectBuffer;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

public class NativeAes implements Aes {
    public static final AesFactory FACTORY = NativeAes::new;
    public static final Supplier<AesFactory> SUPPLIER = () -> FACTORY;

    private long ctx;

    private NativeAes(boolean encrypt, SecretKey key, IvParameterSpec iv) {
        if (!"AES".equals(key.getAlgorithm())) {
            throw new IllegalArgumentException("Invalid key given");
        }
        this.ctx = init(encrypt, key.getEncoded(), iv.getIV());
    }

    private static native long init(boolean encrypt, byte[] key, byte[] iv);

    private static native void free(long ctx);

    @Override
    public void cipher(ByteBuffer input, ByteBuffer output) {
        this.ensureOpen();

        int inPos = input.position();
        int inRem = Math.max(input.limit() - inPos, 0);
        int outPos = output.position();
        int outRem = Math.max(output.limit() - outPos, 0);

        if (inRem < 16) throw new IllegalArgumentException("AES requires at least 16 bytes to encode");
        if (outRem < inRem) throw new IllegalArgumentException("output buffer does not have enough space");

        if (input.isDirect()) {
            long inAddress = ((DirectBuffer) input).address();

            if (output.isDirect()) {
                long outAddress = ((DirectBuffer) output).address();

                this.cipherBufferToBuffer(this.ctx, inAddress, outAddress, inRem);
            } else {
                byte[] outArray = ByteBufferUtils.getBufferArray(output);
                int outOffset = ByteBufferUtils.getBufferOffset(output);

                this.cipherBufferToBytes(this.ctx, inAddress, outArray, outOffset, inRem);
            }
        } else {
            byte[] inArray = ByteBufferUtils.getBufferArray(input);
            int inOffset = ByteBufferUtils.getBufferOffset(input);

            if (output.isDirect()) {
                long outAddress = ((DirectBuffer) output).address();

                this.cipherBytesToBuffer(this.ctx, inArray, inOffset, outAddress, inRem);
            } else {
                byte[] outArray = ByteBufferUtils.getBufferArray(output);
                int outOffset = ByteBufferUtils.getBufferOffset(output);

                this.cipherBytesToBytes(this.ctx, inArray, inOffset, outArray, outOffset, inRem);
            }
        }
    }

    @Override
    public synchronized void free() {
        if (this.ctx != 0) {
            free(this.ctx);
            this.ctx = 0;
        }
    }

    private void ensureOpen() {
        if (this.ctx == 0) throw new IllegalStateException("Native resource has already been freed");
    }

    private native void cipherBytesToBytes(long ctx, byte[] in, int inOff, byte[] out, int outOff, int len);

    private native void cipherBytesToBuffer(long ctx, byte[] in, int inOff, long outAddress, int len);

    private native void cipherBufferToBytes(long ctx, long inAddress, byte[] out, int outOff, int len);

    private native void cipherBufferToBuffer(long ctx, long inAddress, long outAddress, int len);
}
