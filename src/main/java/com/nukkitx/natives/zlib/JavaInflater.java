package com.nukkitx.natives.zlib;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

public class JavaInflater implements Inflater {

    private final java.util.zip.Inflater inflater;

    JavaInflater(boolean nowrap) {
        this.inflater = new java.util.zip.Inflater(nowrap);
    }

    @Override
    public void setInput(ByteBuffer input) {
        if (input.hasArray()) {
            this.inflater.setInput(input.array(), input.arrayOffset(), input.remaining());
        } else {
            byte[] bytes = new byte[input.remaining()];
            input.get(bytes);
            this.inflater.setInput(bytes);
        }
    }

    @Override
    public int inflate(ByteBuffer output) throws DataFormatException {
        if (output.hasArray()) {
            return this.inflater.inflate(output.array(), output.arrayOffset(), output.remaining());
        } else {
            byte[] localBytes = Zlib.WRITE_BYTES.get();

            int startPos = output.position();
            while (output.remaining() > 0 && !this.inflater.finished()) {
                byte[] bytes;
                if (output.remaining() < 8192) {
                    bytes = new byte[output.remaining()];
                } else {
                    bytes = localBytes;
                }
                int result = this.inflater.inflate(bytes);
                output.put(bytes, 0, result);
            }
            return output.position() - startPos;
        }
    }

    @Override
    public int getAdler() {
        return this.inflater.getAdler();
    }

    @Override
    public boolean finished() {
        return this.inflater.finished();
    }

    @Override
    public void reset() {
        this.inflater.reset();
    }

    @Override
    public void free() {
        this.inflater.end();
    }
}
