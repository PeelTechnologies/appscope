/*
 * Copyright (C) 2018 Peel Technologies Inc.
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

import com.google.gson.reflect.TypeToken;
import com.peel.prefs.TypedKey;

public class TypedKeyWithProvider<T> extends TypedKey<T> {
    private final InstanceProvider<T> provider;

    /**
     * By default, a key with provider is non-persistent
     * @param name key name
     * @param clazz type of key
     */
    public TypedKeyWithProvider(String name, Class<T> clazz, InstanceProvider<T> provider) {
        this(name, clazz, provider, AppScope.NON_PERSISTENT);
    }

    public TypedKeyWithProvider(String name, Class<T> clazz, InstanceProvider<T> provider, String... tags) {
        super(name, clazz, tags);
        this.provider = provider;
    }

    public TypedKeyWithProvider(String name, Class<T> clazz, InstanceProvider<T> provider, boolean cacheableInMemory, String... tags) {
        super(name, clazz, cacheableInMemory, tags);
        this.provider = provider;
    }

    public TypedKeyWithProvider(String name, TypeToken<T> type, InstanceProvider<T> provider) {
        this(name, type, provider, AppScope.NON_PERSISTENT);
    }

    public TypedKeyWithProvider(String name, TypeToken<T> type, InstanceProvider<T> provider, String... tags) {
        super(name, type, tags);
        this.provider = provider;
    }

    public TypedKeyWithProvider(String name, TypeToken<T> type, InstanceProvider<T> provider, boolean cacheableInMemory, String... tags) {
        super(name, type, cacheableInMemory, tags);
        this.provider = provider;
    }

    public InstanceProvider<T> getProvider() {
        return provider;
    }
}
