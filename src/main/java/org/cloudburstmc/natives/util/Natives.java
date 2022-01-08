package org.cloudburstmc.natives.util;

import org.cloudburstmc.natives.NativeCode;
import io.netty.util.internal.PlatformDependent;
import org.cloudburstmc.natives.zlib.Java11ZlibProcessor;
import org.cloudburstmc.natives.zlib.JavaZlibProcessor;
import org.cloudburstmc.natives.zlib.LibdeflateZlibProcessor;
import org.cloudburstmc.natives.zlib.ZlibProcessor;

import java.util.function.BooleanSupplier;

public class Natives {
    private static final BooleanSupplier TRUE = () -> true;

//    public static final NativeCode<Crc32C> CRC32C = new NativeCode<>(
//            new NativeCode.Variant<>("Java 9", () -> PlatformUtils.javaVersion() >= 9, Jdk9Crc32C.SUPPLIER),
//            new NativeCode.Variant<>("Pure Java", TRUE, JavaCrc32C.SUPPLIER)
//    );

//    public static final NativeCode<Sha256> SHA_256 = new NativeCode<>(
//            new NativeCode.Variant<>("Native", new LibraryLoader("libnukkit-natives"), NativeSha256.SUPPLIER),
//            new NativeCode.Variant<>("Pure Java", TRUE, JavaSha256.SUPPLIER)
//    );
//
//    public static final NativeCode<AesFactory> AES_CFB8 = new NativeCode<>(
//            new NativeCode.Variant<>("Native", new LibraryLoader("libnukkit-natives"), NativeAes.SUPPLIER),
//            new NativeCode.Variant<>("Java", TRUE, JavaAes.SUPPLIER)
//    );

    public static final NativeCode<ZlibProcessor.Factory> ZLIB = new NativeCode<>(
            new NativeCode.Variant<>("libdeflate", new LibraryLoader("cb_natives"), LibdeflateZlibProcessor.FACTORY),
            new NativeCode.Variant<>("Java 11", () -> PlatformDependent.javaVersion() >= 11, Java11ZlibProcessor.FACTORY),
            new NativeCode.Variant<>("Java", TRUE, JavaZlibProcessor.FACTORY)
    );
}
