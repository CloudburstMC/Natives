package org.cloudburstmc.natives.zlib;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

import java.util.zip.DataFormatException;

public interface ZlibProcessor extends ReferenceCounted {

    int CHUNK_BYTES = 8192;

    void deflate(ByteBuf input, ByteBuf output, int level) throws DataFormatException;

    void inflate(ByteBuf input, ByteBuf output, int limit) throws DataFormatException;

    interface Factory {
        ZlibProcessor newInstance(ZlibType type);
    }
}
