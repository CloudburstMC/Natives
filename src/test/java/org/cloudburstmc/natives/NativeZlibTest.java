package org.cloudburstmc.natives;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.natives.util.Natives;
import org.cloudburstmc.natives.zlib.LibdeflateZlibProcessor;
import org.cloudburstmc.natives.zlib.ZlibProcessor;
import org.cloudburstmc.natives.zlib.ZlibType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.zip.DataFormatException;

public class NativeZlibTest {

    private static final ZlibProcessor.Factory FACTORY = Natives.ZLIB.get();
    private static final NativeCode.Variant<ZlibProcessor.Factory>[] FACTORIES = Natives.ZLIB.getVariants();

    @Test
    public void testZlib() throws DataFormatException {
        System.out.println("Running test");

        for (int i = FACTORIES.length - 1; i >= 0; i--) {
            test(FACTORIES[i]);

            if (FACTORIES[i].getFactory() == FACTORY) break;
        }
    }

    private void test(NativeCode.Variant<ZlibProcessor.Factory> variant) throws DataFormatException {
        System.out.println("Testing: " + variant.getName());
        long start = System.currentTimeMillis();

        byte[] dataBuf = new byte[1 << 22]; // 2 megabytes
        new Random().nextBytes(dataBuf);

        ZlibProcessor zlib = variant.getFactory().newInstance(ZlibType.DEFLATE);

        ByteBuf originalBuf = Unpooled.directBuffer(dataBuf.length);
        originalBuf.writeBytes(dataBuf);

        ByteBuf compressed = Unpooled.directBuffer(dataBuf.length << 1);

        zlib.deflate(originalBuf, compressed , 9);

        // Repeat here to test .reset()
        originalBuf = Unpooled.directBuffer(dataBuf.length);
        originalBuf.writeBytes(dataBuf);

        compressed = Unpooled.directBuffer(dataBuf.length << 1);

        zlib.deflate(originalBuf, compressed, 9);

        ByteBuf uncompressed = Unpooled.directBuffer(dataBuf.length);

        zlib.inflate(compressed, uncompressed, 8 * 1024 * 1024);

        byte[] check = new byte[uncompressed.readableBytes()];
        uncompressed.readBytes(check);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Took: " + elapsed + "ms");

        Assertions.assertArrayEquals(dataBuf, check, "Results do not match");
    }
}
