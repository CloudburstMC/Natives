package com.nukkitx.natives.util;

import com.nukkitx.natives.NativeCode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class LibraryLoader implements BooleanSupplier {
    private static final Set<Path> toDelete = new HashSet<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Path path : toDelete) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            }
        }));
    }

    private final String name;

    public LibraryLoader(String name) {
        this.name = name;
    }

    private static String getExtension(String name) {
        String extension = "";

        int i = name.lastIndexOf('.');
        int p = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));

        if (i > p) {
            extension = name.substring(i + 1);
        }
        return extension;
    }

    @Override
    public boolean getAsBoolean() {
        final String fullName = this.name + '-' + PlatformUtils.OPERATING_SYSTEM + '-' +
                PlatformUtils.ARCHITECTURE;
        final String tmpName = "native-" + fullName;
        final String libraryName = System.mapLibraryName(fullName);
        final String libraryExtension = getExtension(libraryName);

        try {
            // Check if the temporary file exists
            System.loadLibrary(tmpName);
            return true;
        } catch (Throwable ignored) {
        }

        try (InputStream resourceStream = NativeCode.class.getClassLoader()
                .getResourceAsStream(libraryName)) {
            if (resourceStream == null) {
                // Likely unavailable for the arch/os
                return false;
            }
            Path tempPath = Files.createTempFile(tmpName, libraryExtension);
            Files.copy(resourceStream, tempPath);
            toDelete.add(tempPath);
            System.loadLibrary(this.name);
            return true;
        } catch (Throwable e) {
            System.out.println("Could not load native library: " + e.getMessage());
        }
        return false;
    }
}
