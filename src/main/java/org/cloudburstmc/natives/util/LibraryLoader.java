package org.cloudburstmc.natives.util;

import io.netty.util.internal.NativeLibraryLoader;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.function.BooleanSupplier;

public class LibraryLoader implements BooleanSupplier {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(LibraryLoader.class);

    private final String name;

    public LibraryLoader(String name) {
        this.name = name;
    }

    @Override
    public boolean getAsBoolean() {
        String libraryName = this.name + '_' + PlatformDependent.normalizedOs() + '_' + PlatformDependent.normalizedArch();

        ClassLoader cl = PlatformDependent.getClassLoader(LibraryLoader.class);
        try {
            // Check if the temporary file exists
            NativeLibraryLoader.load(libraryName, cl);
            return true;
        } catch (UnsatisfiedLinkError e) {
            logger.debug("Failed to load {}", libraryName, e);
        }
        return false;
    }
}
