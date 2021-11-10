package org.cloudburstmc.natives.zlib;

import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class JavaZlibProcessor extends AbstractReferenceCounted implements ZlibProcessor {

    public static final Factory FACTORY = JavaZlibProcessor::new;

    private final byte[] chunkBytes = new byte[CHUNK_BYTES];
    private final Deflater[] deflaters = new Deflater[10];
    private final Inflater inflater;
    private byte[] inputBytes = new byte[CHUNK_BYTES];

    private JavaZlibProcessor(ZlibType type) {
        for (int level = 0; level < this.deflaters.length; level++) {
            this.deflaters[level] = new Deflater(level, type == ZlibType.DEFLATE);
        }
        this.inflater = new Inflater(type == ZlibType.DEFLATE);
    }

    @Override
    public void deflate(ByteBuf input, ByteBuf output, int level) {
        int inputLength = input.readableBytes();
        this.inputBytes = getAndResize(this.inputBytes, inputLength);
        input.readBytes(this.inputBytes, 0, inputLength);

        Deflater deflater = this.deflaters[level];
        try {
            deflater.setInput(this.inputBytes, 0, inputLength);
            deflater.finish();

            while (!deflater.finished()) {
                int written = deflater.deflate(this.chunkBytes);
                output.writeBytes(this.chunkBytes, 0, written);
            }
        } finally {
            deflater.reset(); // Make sure we reset even if an exception is thrown
        }
    }

    @Override
    public void inflate(ByteBuf input, ByteBuf output, int limit) throws DataFormatException {
        if (input.readableBytes() > limit) {
            throw new DataFormatException("Decompressed data is larger than limit of " + limit + " bytes!");
        }
        int inputLength = input.readableBytes();
        this.inputBytes = getAndResize(this.inputBytes, inputLength);
        input.readBytes(this.inputBytes, 0, inputLength);

        Inflater inflater = this.inflater;
        try {
            inflater.setInput(this.inputBytes, 0, inputLength);

            while (!inflater.finished()) {
                int read = inflater.inflate(this.chunkBytes);
                output.writeBytes(this.chunkBytes, 0, read);
                if (read <= 0) {
                    break;
                } else if (output.writerIndex() > limit) {
                    throw new DataFormatException("Uncompressed data is larger than the limit of " + limit + " bytes!");
                }
            }
        } finally {
            inflater.reset(); // Make sure we reset even if an exception is thrown
        }
    }

    @Override
    protected void deallocate() {
        for (Deflater deflater : this.deflaters) {
            deflater.end();
        }
        this.inflater.end();
    }

    @Override
    public JavaZlibProcessor touch(Object o) {
        return this;
    }

    private static byte[] getAndResize(byte[] bytes, int neededCapacity) {
        if (neededCapacity > bytes.length) {
            return new byte[neededCapacity];
        }
        return bytes;
    }
}
