package org.cloudburstmc.natives;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.natives.util.Natives;
import org.cloudburstmc.natives.zlib.LibdeflateZlibProcessor;
import org.cloudburstmc.natives.zlib.ZlibProcessor;
import org.cloudburstmc.natives.zlib.ZlibType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.zip.DataFormatException;

public class NativeZlibTest {

    private static final byte[] TEST_DATA;

    static {
        try {
            // Even though the language level is Java 8, we compile with Java 11 so this can be safely ignored.
            //noinspection Since15
            TEST_DATA = Objects.requireNonNull(NativeZlibTest.class.getClassLoader().getResourceAsStream("bedrock-packet.dat")).readAllBytes();
        } catch (Exception e) {
            throw new AssertionError("Unable to load data", e);
        }
    }

    @Test
    public void testZlib() throws DataFormatException, IOException {
        for (NativeCode.Variant<ZlibProcessor.Factory> variant : Natives.ZLIB.getAvailableVariants()) {
            test(variant);
        }
    }

    private void test(NativeCode.Variant<ZlibProcessor.Factory> variant) throws DataFormatException {
        System.out.println("Testing: " + variant.getName());
        byte[] dataBuf = TEST_DATA;
        ZlibProcessor zlib = variant.getFactory().newInstance(ZlibType.DEFLATE);

        long start = System.nanoTime();

        ByteBuf originalBuf = Unpooled.directBuffer();
        originalBuf.writeBytes(dataBuf);

        ByteBuf compressed = Unpooled.directBuffer(originalBuf.readableBytes());

        Assertions.assertTrue(zlib.deflate(originalBuf, compressed, 7), "Could not decompress");

        // Repeat here to test .reset()
        originalBuf = Unpooled.directBuffer(originalBuf.readableBytes());
        originalBuf.writeBytes(dataBuf);

        compressed = Unpooled.directBuffer(originalBuf.readableBytes());

        Assertions.assertTrue(zlib.deflate(originalBuf, compressed, 7), "Could not decompress");

        ByteBuf uncompressed = Unpooled.directBuffer(originalBuf.readableBytes());

        zlib.inflate(compressed, uncompressed, 8 * 1024 * 1024);

        byte[] check = new byte[uncompressed.readableBytes()];
        uncompressed.readBytes(check);

        double elapsed = (System.nanoTime() - start) / 1_000_000D;
        System.out.println("Took: " + elapsed + "ms");

        Assertions.assertArrayEquals(dataBuf, check, "Results do not match");
    }
}
