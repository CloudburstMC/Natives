package org.cloudburstmc.natives.zlib;

import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Java11ZlibProcessor extends AbstractReferenceCounted implements ZlibProcessor {

    public static final Factory FACTORY = Java11ZlibProcessor::new;

    private final Deflater[] deflaters = new Deflater[10];
    private final java.util.zip.Inflater inflater;

    private Java11ZlibProcessor(ZlibType type) {
        for (int level = 0; level < this.deflaters.length; level++) {
            this.deflaters[level] = new Deflater(level, type == ZlibType.DEFLATE);
        }
        this.inflater = new Inflater(type == ZlibType.DEFLATE);
    }

    @Override
    public void deflate(ByteBuf input, ByteBuf output, int level) throws DataFormatException {
        Deflater deflater = this.deflaters[level];
        try {
            for (ByteBuffer buffer : input.nioBuffers()) {
                deflater.setInput(buffer);

                while (!deflater.needsInput()) {
                    output.ensureWritable(CHUNK_BYTES);
                    ByteBuffer outBuffer = output.nioBuffer(output.writerIndex(), CHUNK_BYTES);
                    int written = deflater.deflate(outBuffer);
                    output.writerIndex(output.writerIndex() + written);
                }
            }
            deflater.finish();

            while (!deflater.finished()) {
                output.ensureWritable(CHUNK_BYTES);
                ByteBuffer outBuffer = output.nioBuffer(output.writerIndex(), CHUNK_BYTES);
                int written = deflater.deflate(outBuffer);
                output.writerIndex(output.writerIndex() + written);
            }
        } finally {
            deflater.reset();
        }
    }

    @Override
    public void inflate(ByteBuf input, ByteBuf output, int limit) throws DataFormatException {
        int startIndex = output.writerIndex();

        Inflater inflater = this.inflater;
        try {
            for (ByteBuffer buffer : input.nioBuffers()) {
                inflater.setInput(buffer);

                while (!inflater.needsInput()) {
                    output.ensureWritable(CHUNK_BYTES);
                    ByteBuffer outBuffer = output.nioBuffer(output.writerIndex(), CHUNK_BYTES);
                    int written = inflater.inflate(outBuffer);
                    if (written <= 0) break;
                    output.writerIndex(output.writerIndex() + written);
                    if (output.writerIndex() - startIndex >= limit) {
                        throw new DataFormatException("Uncompressed data is larger than max size of " + limit + " bytes!");
                    }
                }
            }
            if (!inflater.finished()) {
                throw new DataFormatException("Test");
            }
        } finally {
            inflater.reset();
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
    public Java11ZlibProcessor touch(Object o) {
        return this;
    }
}
