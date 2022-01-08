package org.cloudburstmc.natives;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.natives.util.Natives;
import org.cloudburstmc.natives.zlib.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

public class ZlibBenchmarkTest {

    @Test
    @DisplayName("Zlib Benchmark")
    public void testBenchmark() throws Exception {
        // Check what variants are available.
        List<String> includes = Natives.ZLIB.getAvailableVariants().stream()
                .map(variant -> variant.getName().toLowerCase(Locale.ROOT).replace(' ', '_'))
                .collect(Collectors.toList());

        ChainedOptionsBuilder opt = new OptionsBuilder()
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(5)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(5)
                .threads(2)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true);

        for (String include : includes) {
            opt.include(this.getClass().getName() + ".deflate_" + include);
            opt.include(this.getClass().getName() + ".inflate_" + include);
        }

        new Runner(opt.build()).run();
    }

    @State(Scope.Thread)
    public static class BenchmarkState {
        final int level = 7;
        ZlibProcessor java;
        ZlibProcessor java11;
        ZlibProcessor libdeflate;
        ByteBuf input;
        ByteBuf output;
        ByteBuf decompressed;

        @Setup(Level.Trial)
        public void initialize() {
            Function<NativeCode.Variant<ZlibProcessor.Factory>, ZlibProcessor> map = v -> v.getFactory().newInstance(ZlibType.DEFLATE);
            this.java = Natives.ZLIB.getVariant("Java").map(map).orElse(null);
            this.java11 = Natives.ZLIB.getVariant("Java 11").map(map).orElse(null);
            this.libdeflate = Natives.ZLIB.getVariant("libdeflate").map(map).orElse(null);

            try (InputStream stream = ZlibBenchmarkTest.class.getClassLoader().getResourceAsStream("bedrock-packet.dat")) {
                this.input = Unpooled.directBuffer();
                int nRead;
                byte[] data = new byte[16384];

                while ((nRead = stream.read(data, 0, data.length)) != -1) {
                    this.input.ensureWritable(nRead);
                    this.input.writeBytes(data, 0, nRead);
                }
                this.decompressed = Unpooled.directBuffer();
                this.java.deflate(input.slice(), decompressed, 9);

            } catch (IOException | DataFormatException e) {
                throw new AssertionError("Unable to load benchmark NBT");
            }
        }

        @Setup(Level.Invocation)
        public void lateInit() {
            if (this.output != null) {
                this.output.release();
            }
            this.output = Unpooled.directBuffer();
        }
    }

    @Benchmark
    public void inflate_java(BenchmarkState state, Blackhole bh) throws DataFormatException {
        state.java.inflate(state.decompressed.slice(), state.output.writerIndex(0).readerIndex(0), 8 * 1024 * 1024);
    }

    @Benchmark
    public void deflate_java(BenchmarkState state, Blackhole bh) throws DataFormatException {
        state.java.deflate(state.input.slice(), state.output.writerIndex(0).readerIndex(0), 7);
    }

    @Benchmark
    public void inflate_java_11(BenchmarkState state, Blackhole bh) throws DataFormatException {
        state.java11.inflate(state.decompressed.slice(), state.output.writerIndex(0).readerIndex(0), 8 * 1024 * 1024);
    }

    @Benchmark
    public void deflate_java_11(BenchmarkState state, Blackhole bh) throws DataFormatException {
        state.java11.deflate(state.input.slice(), state.output.writerIndex(0).readerIndex(0), 7);
    }

    @Benchmark
    public void inflate_libdeflate(BenchmarkState state, Blackhole bh) throws DataFormatException {
        state.libdeflate.inflate(state.decompressed.slice(), state.output.writerIndex(0).readerIndex(0), 8 * 1024 * 1024);
    }

    @Benchmark
    public void deflate_libdeflate(BenchmarkState state, Blackhole bh) throws DataFormatException {
        state.libdeflate.deflate(state.input.slice(), state.output.writerIndex(0).readerIndex(0), 7);
    }
}
