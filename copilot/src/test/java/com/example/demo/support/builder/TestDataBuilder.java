package com.example.demo.support.builder;

/**
 * Base functional interface for all test data builders.
 * Provides {@link #build()} and {@link #buildAndPersist()} methods.
 *
 * @param <T> the type of object to build
 * @author Demo Team
 * @since 1.0.0
 */
@FunctionalInterface
public interface TestDataBuilder<T> {

    /**
     * Builds the object without persisting it.
     *
     * @return the built object
     */
    T build();

    /**
     * Builds and persists the object.
     * Default implementation delegates to {@link #build()}.
     *
     * @return the built and persisted object
     */
    default T buildAndPersist() {
        return build();
    }
}
