package com.nukkitx.natives.util;

import com.nukkitx.natives.NativeCode;
import com.nukkitx.natives.aes.AesFactory;
import com.nukkitx.natives.aes.JavaAes;
import com.nukkitx.natives.aes.NativeAes;
import com.nukkitx.natives.crc32c.Crc32C;
import com.nukkitx.natives.crc32c.JavaCrc32C;
import com.nukkitx.natives.crc32c.Jdk9Crc32C;
import com.nukkitx.natives.sha256.JavaSha256;
import com.nukkitx.natives.sha256.NativeSha256;
import com.nukkitx.natives.sha256.Sha256;
import com.nukkitx.natives.zlib.Zlib;

import java.util.function.BooleanSupplier;

public class Natives {
    private static final BooleanSupplier TRUE = () -> true;

    public static final NativeCode<Crc32C> CRC32C = new NativeCode<>(
            new NativeCode.Variant<>("Java 9", () -> PlatformUtils.javaVersion() >= 9, Jdk9Crc32C.SUPPLIER),
            new NativeCode.Variant<>("Pure Java", TRUE, JavaCrc32C.SUPPLIER)
    );

    public static final NativeCode<Sha256> SHA_256 = new NativeCode<>(
            new NativeCode.Variant<>("Native", new LibraryLoader("libnukkit-natives"), NativeSha256.SUPPLIER),
            new NativeCode.Variant<>("Pure Java", TRUE, JavaSha256.SUPPLIER)
    );

    public static final NativeCode<AesFactory> AES_CFB8 = new NativeCode<>(
            new NativeCode.Variant<>("Native", new LibraryLoader("libnukkit-natives"), NativeAes.SUPPLIER),
            new NativeCode.Variant<>("Java", TRUE, JavaAes.SUPPLIER)
    );

    public static final NativeCode<Zlib> ZLIB = new NativeCode<>(
            new NativeCode.Variant<>("Java 11", () -> PlatformUtils.javaVersion() >= 11, Zlib.JAVA_11),
            new NativeCode.Variant<>("Java", TRUE, Zlib.JAVA)
    );
}
