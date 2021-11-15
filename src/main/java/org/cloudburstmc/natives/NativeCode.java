package org.cloudburstmc.natives;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class NativeCode<T> implements Supplier<T> {

    private final Variant<T>[] variants;
    private final Map<String, Variant<T>> availableVariants = new LinkedHashMap<>();
    private final Variant<T> defaultVariant;

    @SafeVarargs
    public NativeCode(Variant<T>... variants) {
        Objects.requireNonNull(variants, "variants");
        if (variants.length < 2) throw new IllegalArgumentException("At least 2 variants must be specified");

        this.variants = variants;

        Variant<T> defaultVariant = null;

        for (Variant<T> variant : this.variants) {
            if (variant.availability.getAsBoolean()) {
                if (defaultVariant == null) {
                    defaultVariant = variant;
                }

                this.availableVariants.put(variant.name, variant);
            }
        }
        this.defaultVariant = defaultVariant;
        if (defaultVariant == null) {
            throw new IllegalStateException("No variants were able to load");
        }
    }

    @Override
    public T get() {
        return this.defaultVariant.factory;
    }

    public String getVariantName() {
        return this.defaultVariant.name;
    }

    public Variant<T>[] getVariants() {
        return variants;
    }

    public Optional<Variant<T>> getVariant(String name) {
        return Optional.ofNullable(availableVariants.get(name));
    }

    public Collection<Variant<T>> getAvailableVariants() {
        return Collections.unmodifiableCollection(availableVariants.values());
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

        public String getName() {
            return name;
        }

        public T getFactory() {
            return factory;
        }
    }
}
