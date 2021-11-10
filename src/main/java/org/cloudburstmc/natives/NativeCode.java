package org.cloudburstmc.natives;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class NativeCode<T> implements Supplier<T> {

    private final Variant<T>[] variants;
    private final Variant<T> loadedVariant;

    @SafeVarargs
    public NativeCode(Variant<T>... variants) {
        Objects.requireNonNull(variants, "variants");
        if (variants.length < 2) throw new IllegalArgumentException("At least 2 variants must be specified");

        this.variants = variants;

        for (Variant<T> variant : this.variants) {
            if (variant.availability.getAsBoolean()) {
                this.loadedVariant = variant;
                return;
            }
        }
        throw new IllegalStateException("No variants were able to load");
    }

    @Override
    public T get() {
        return this.loadedVariant.factory;
    }

    public String getVariantName() {
        return this.loadedVariant.name;
    }

    public Variant<T>[] getVariants() {
        return variants;
    }

    public static class Variant<T> {
        private final String name;
        private final BooleanSupplier availability;
        private final T factory;

        public Variant(String name, BooleanSupplier availability, T factory) {
            this.name = name;
            this.availability = availability;
            this.factory = factory;
        }
    }
}
