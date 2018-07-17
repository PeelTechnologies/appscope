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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.gson.Gson;
import com.peel.prefs.Prefs;
import com.peel.prefs.TypedKey;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * This class provides services to hold data in application scope. It is used to pass global
 * information around across fragments without making everything parcelable. This is a singleton.
 *
 * @author Inderjeet Singh
 */
public final class AppScope {
    public static final String SURVIVE_RESET = "surviveReset";
    /** The addition/removal of these keys will not be reported back in the listener */
    public static final String NON_PERSISTENT = "nonPersistent";


    static final String DEFAULT_USER_PREFS_FILE = "user_prefs";
    static final String DEFAULT_APP_PREFS_FILE = "app_prefs";

    @SuppressLint("StaticFieldLeak")
    private static Prefs userPrefs;
    @SuppressLint("StaticFieldLeak")
    private static Prefs appPrefs;
    private static final Map<TypedKey<?>, Object> nonPersistentPrefs = new ConcurrentHashMap<>();
    private static final Set<TypedKeyWithProvider<?>> keysWithProviders = new CopyOnWriteArraySet<>();

    public static void init(Context context, Gson gson) {
        init(context, gson, DEFAULT_USER_PREFS_FILE, DEFAULT_APP_PREFS_FILE, 20);
    }

    public static void init(Context context, Gson gson,
            String persistentPrefsFileName, String configPrefsFileName, int cacheSize) {
        userPrefs = new Prefs(context, gson, persistentPrefsFileName, cacheSize);
        appPrefs = new Prefs(context, gson, configPrefsFileName, cacheSize);
    }

    public static void addListener(Prefs.EventListener listener) {
        userPrefs.addListener(listener);
        appPrefs.addListener(listener);
        // non persistent items can't be listened to
    }

    public static void removeListener(Prefs.EventListener listener) {
        userPrefs.removeListener(listener);
        appPrefs.removeListener(listener);
    }

    public static Context context() {
        return userPrefs.context();
    }

    public static<T> void register(TypedKeyWithProvider<T> key) {
        keysWithProviders.add(key);
    }

    /** Deprecated. Use {@code #put(TypedKey, Object)} instead. */
    @Deprecated
    public static <T> void bind(TypedKey<T> key, T value) {
        put(key, value);
    }

    public static <T> void put(TypedKey<T> key, T value) {
        if (key instanceof TypedKeyWithProvider) {
            TypedKeyWithProvider<T> key1 = (TypedKeyWithProvider<T>) key;
            InstanceProvider<T> provider = key1.getProvider();
            if (provider == null) throw new IllegalArgumentException(key + " must have a non-null provider!");
            provider.update(value);
            if (!keysWithProviders.contains(key1)) keysWithProviders.add(key1);
        } else if (key.containsTag(NON_PERSISTENT)) {
            nonPersistentPrefs.put(key, value);
        } else if (key.containsTag(SURVIVE_RESET)) {
            appPrefs.put(key, value);
        } else {
            userPrefs.put(key, value);
        }
    }

    public static <T> void put(String keyName, Class<T> keyClass, T value) {
        userPrefs.put(keyName, keyClass, value);
    }

    /** Use {@code #contains(TypedKey) instead. */
    @Deprecated
    public static <T> boolean has(TypedKey<T> key) {
        return contains(key);
    }

    public static <T> boolean contains(TypedKey<T> key) {
        return nonPersistentPrefs.containsKey(key) || userPrefs.contains(key)
                || appPrefs.contains(key) || key instanceof TypedKeyWithProvider;
    }

    public static <T> boolean contains(String keyName, Class<T> keyClass) {
        TypedKey<T> key = new TypedKey<T>(keyName, keyClass);
        return has(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(TypedKey<T> key) {
        T instance = null;
        if (key instanceof TypedKeyWithProvider) {
            instance = ((TypedKeyWithProvider<T>) key).getProvider().get();
        }
        if (instance == null) {
            instance = (T) nonPersistentPrefs.get(key);
            if (instance == null) {
                if (userPrefs.contains(key)) instance = userPrefs.get(key); // boolean values get defaulted to false, we don't want that
                if (instance == null) instance = appPrefs.get(key);
            }
        }
        return instance;
    }

    public static <T> T get(String keyName, Class<T> keyClass) {
        TypedKey<T> key = new TypedKey<T>(keyName, keyClass);
        return get(key);
    }

    public static <T> T get(TypedKey<T> key, T defaultValue) {
        return has(key) ? get(key) : defaultValue;
    }

    public static <T> T get(String keyName, Class<T> keyClass, T defaultValue) {
        TypedKey<T> key = new TypedKey<T>(keyName, keyClass);
        return get(key, defaultValue);
    }

    /**
     * Removes a provider as well as any registered instances with this name
     * @param <T> the type of the {@code TypedKey}
     * @param key the key that was previously bound as an instance or a provider.
     *  If the key was not bound previously, nothing is done
     */
    public static <T> void remove(TypedKey<T> key) {
        nonPersistentPrefs.remove(key);
        userPrefs.remove(key);
        appPrefs.remove(key);
        keysWithProviders.remove(key);
    }

    public static <T> void remove(String keyName, Class<T> keyClass) {
        TypedKey<T> key = new TypedKey<T>(keyName, keyClass);
        remove(key);
    }

    public static void reset() {
        reset(false);
    }

    private static synchronized void reset(boolean reset) {
        for (TypedKeyWithProvider<?> key : keysWithProviders) {
            if (reset || !key.containsTag(SURVIVE_RESET)) {
                try {
                    InstanceProvider<?> provider = key.getProvider();
                    if (provider != null) provider.update(null);
                } catch (Exception ignored) {}
            }
        }
        userPrefs.clear();

        List<TypedKey<?>> toBeRemoved = new ArrayList<>();
        for (Map.Entry<TypedKey<?>, Object> entry : nonPersistentPrefs.entrySet()) {
            TypedKey<?> key = entry.getKey();
            if (!key.containsTag(SURVIVE_RESET)) {
                toBeRemoved.add(key);
            }
        }
        for (TypedKey<?> key : toBeRemoved) {
            nonPersistentPrefs.remove(key);
        }
    }

    public static final class TestAccess {
        /**
         * initializes AppScope by clearing out any past settings. This is useful for tests
         * to ensure AppScope side-effects from other test invocations are cleared out for
         * this test.
         *
         * @param context the context to set while reinitializing AppScope
         * @param gson the Gson instance to set while reinitializing AppScope
         */
        public static void init(Context context, Gson gson) {
            AppScope.init(context, gson, DEFAULT_USER_PREFS_FILE, DEFAULT_APP_PREFS_FILE, 10);
            reset();
        }

        public static void reset() {
            AppScope.reset(true);
            nonPersistentPrefs.clear();
            userPrefs.clear();
            appPrefs.clear();
        }
    }
}
