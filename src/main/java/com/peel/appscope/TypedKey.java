/*
 * Copyright (C) 2017 Peel Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.peel.appscope;

import java.lang.reflect.Type;

/**
 * A typed key
 *
 * @param <T> Intended type of the content for the key
 * @author Inderjeet Singh
 */
public class TypedKey<T> {

    public static class Builder<R> {
        protected final String name;
        protected final Type type;
        protected boolean config;
        protected boolean persist;
        protected boolean cacheableInMemory = true;

        public Builder(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public Builder(String name, Class<R> clazz) {
            this(name, (Type) clazz);
        }

        /**
         * Configure whether {@ link AppScope#reset()} will wipe out this key or not
         * @param survive If true, {@ link AppScope#reset()} will not delete this key
         * @return the builder
         */
        public Builder<R> survivesReset() {
            this.config = true;
            return this;
        }

        /**
         * Call this method to indicate that this key is to be persisted on disk.
         *
         * @return the builder
         */
        public Builder<R> persist() {
            return persist(true);
        }

        /**
         * Call this method to indicate that this key is to be persisted on disk.
         *
         * @param cacheableInMemory whether this key/value can be cached in memory.
         *   If false, the key is only stored in prefs and reloaded from it every time.
         *   Most keys should set this to true.
         * @return the builder
         */
        public Builder<R> persist(boolean cacheableInMemory) {
            this.persist = true;
            this.cacheableInMemory = cacheableInMemory;
            return this;
        }

        public TypedKey<R> build() {
            return new TypedKey<R>(type, name, config, persist, cacheableInMemory);
        }
    }

    private final String name;
    private final Type type;
    private final boolean config;
    private final boolean persist;
    private final boolean cacheableInMemory;

    /**
     * @param name Ensure that this name is Unique.
     * @param clazz the type of this key
     */
    public TypedKey(String name, Class<T> clazz) {
        this(name, clazz, false, false);
    }

    /**
     * @param name Ensure that this name is unique
     * @param clazz the type of this key
     * @param config If true, this property is considered to be an app configuration property
     *   that will not be erased on app reset.
     * @param persist whether to save this property on disk as JSON and reload on app restart.
     */
    public TypedKey(String name, Class<T> clazz, boolean config, boolean persist) {
        this(clazz, name, config, persist, true);
    }

    protected TypedKey(Type type, String name, boolean config, boolean persist, boolean cacheableInMemory) {
        if (!cacheableInMemory && !persist) { // Precondition check
            throw new IllegalArgumentException("cacheableInMemory can be false only if persist is set to true!");
        }
        this.name = name;
        this.type = type;
        this.config = config;
        this.persist = persist;
        this.cacheableInMemory = cacheableInMemory;
    }

    public String getName() {
        return name;
    }

    public Type getTypeOfValue() {
        return type;
    }

    public boolean isConfigType() {
        return config;
    }

    public boolean isCacheableInMemory() {
        return cacheableInMemory;
    }

    public boolean isPersistable() {
        return persist;
    }

    public boolean hasProvider() {
        return getProvider() != null;
    }

    /**
     * Override this method to provide a custom provider
     * @return the custom {@code InstanceProvider} associated with this key
     */
    public InstanceProvider<T> getProvider() {
        return null;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        TypedKey<?> other = (TypedKey<?>) obj;
        return name.equals(other.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
