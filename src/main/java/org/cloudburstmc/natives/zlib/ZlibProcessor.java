package org.cloudburstmc.natives.zlib;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

import java.util.zip.DataFormatException;

public interface ZlibProcessor extends ReferenceCounted {

    int MIN_BLOCK_LENGTH = 10000;
    int OUTPUT_END_PADDING = 8;
    int CHUNK_BYTES = 8192;

    boolean deflate(ByteBuf input, ByteBuf output, int level) throws DataFormatException;

    void inflate(ByteBuf input, ByteBuf output, int limit) throws DataFormatException;

    interface Factory {
        ZlibProcessor newInstance(ZlibType type);
    }

    default int getCompressionBound(int length) {
        int maxBlocks = Math.max((length - 1) / MIN_BLOCK_LENGTH + 1, 1);
        return (5 * maxBlocks) + length + 1 + OUTPUT_END_PADDING;
    }
}
