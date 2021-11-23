package org.cloudburstmc.natives.zlib;

import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;

import java.util.zip.DataFormatException;

public class LibdeflateZlibProcessor extends AbstractReferenceCounted implements ZlibProcessor {

    public static final Factory FACTORY = LibdeflateZlibProcessor::new;

    private final long ctx;

    private LibdeflateZlibProcessor(ZlibType type) {
        this.ctx = init(type == ZlibType.DEFLATE);
    }

    @Override
    public boolean deflate(ByteBuf input, ByteBuf output, int level) throws DataFormatException {
        if (level < 0 || level > 12) {
            throw new IllegalArgumentException("Compression level was out of bounds. Expected a value between 0-12 but got " + level);
        }
        // Extracted from libdeflate so we don't have to execute the JNI method.
        if (input.readableBytes() < 8 || input.readableBytes() < 56 - (level * 4)) {
            return false;
        }

        // ByteBuf#memoryAddress will throw an exception if the buffer is not native.
        long inAddress = input.memoryAddress() + input.readerIndex();
        long outAddress = output.memoryAddress() + output.writerIndex();

        int written = deflate(this.ctx, inAddress, input.readableBytes(), outAddress, output.writableBytes(), level);
        if (written == 0) {
            return false;
        }
        output.writerIndex(output.writerIndex() + written);
        return true;
    }

    @Override
    public void inflate(ByteBuf input, ByteBuf output, int limit) throws DataFormatException {
        while (true) {
            // ByteBuf#memoryAddress will throw an exception if the buffer is not native.
            long inAddress = input.memoryAddress() + input.readerIndex();
            long outAddress = output.memoryAddress() + output.writerIndex();

            int written = inflate(this.ctx, inAddress, input.readableBytes(), outAddress, output.writableBytes());
            if (written >= 0) {
                output.writerIndex(output.writerIndex() + written);
                return; // Data written successfully
            }
            if (output.writableBytes() < limit) {
                output.ensureWritable(Math.min(output.writableBytes() << 1, limit)); // Increase output size and try again.
            } else {
                break;
            }
        }
        throw new IllegalArgumentException("Inflated buffer size exceeds limit of " + limit + " bytes");
    }

    @Override
    protected void deallocate() {
        free(this.ctx);
    }

    @Override
    public LibdeflateZlibProcessor touch(Object o) {
        return this;
    }

    private static native long init(boolean nowrap);

    private static native void free(long ctx);

    private static native int deflate(long ctx, long inAddress, int inLength, long outAddress, int outLength, int level);

    private static native int inflate(long ctx, long inAddress, int inLength, long outAddress, int outLength);
}
