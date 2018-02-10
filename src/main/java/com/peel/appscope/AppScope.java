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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class provides services to hold data in application scope. It is used to pass global
 * information around across fragments without making everything parcelable. This is a singleton.
 *
 * @author Inderjeet Singh
 */
public final class AppScope {
    @SuppressWarnings("rawtypes")
    private static final Map<TypedKey, Object> instances = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private static final Map<TypedKey, Object> providers = new ConcurrentHashMap<>();
    private static Context context;
    private static Gson gson;
    private static String prefsClearOnResetFileName;
    private static String prefsPersistOnResetFileName;

    public static void configure(Context context, Gson gson) {
    	configure(context, gson,"persistent_props", "config_props");
    }

    public static void configure(Context context, Gson gson, String prefsClearOnResetFileName,
            String prefsPersistOnResetFileName) {
        AppScope.context = context;
        AppScope.gson = gson;
        AppScope.prefsClearOnResetFileName = prefsClearOnResetFileName;
        AppScope.prefsPersistOnResetFileName = prefsPersistOnResetFileName;
    }

    public static Context context() {
    	return context;
    }

    public static <T> void bind(TypedKey<T> key, T value) {
        instances.put(key, value);
        if (key.isPersistable()) {
            if (key.hasProvider()) { // delegate to the provider
                key.getProvider().update(value);
            } else {
                String json = gson.toJson(value);
                SharedPreferences prefs = getPrefs(key.isConfigType());
                prefs.edit().putString(key.getName(), json).apply();
            }
        }
    }

    private static SharedPreferences getPrefs(boolean configType) {
        if (configType) {
            return context.getSharedPreferences(prefsClearOnResetFileName, Context.MODE_PRIVATE);
        } else {
            return context.getSharedPreferences(prefsPersistOnResetFileName, Context.MODE_PRIVATE);
        }
    }

    public static <T> void bindProvider(TypedKey<T> key, InstanceProvider<T> value) {
        // Keys bound with providers shouldn't be persistable since the provider is responsible
        // for providing the value.
        if(key.isPersistable()) throw new IllegalArgumentException(key + " can't be persistable for bindProvider() to work");
        providers.put(key, value);
    }

    /**
     * Removes a provider as well as any registered instances with this name
     */
    public static <T> void remove(TypedKey<T> key) {
        instances.remove(key);
        providers.remove(key);

        if (key.isPersistable()) {
            SharedPreferences prefs = getPrefs(key.isConfigType());
            prefs.edit().remove(key.getName()).apply();
        }
    }

    public static <T> boolean has(TypedKey<T> key) {
        boolean has = instances.containsKey(key) || providers.containsKey(key) || key.hasProvider();
        if (!has && key.isPersistable()) {
            SharedPreferences prefs = getPrefs(key.isConfigType());
            has = prefs.contains(key.getName());
        }
        return has;
    }

    /**
     * Returns a provider for the key if it was registered.
     */
    @SuppressWarnings("unchecked")
    public static <T> InstanceProvider<T> getProvider(TypedKey<T> key) {
        if (key.hasProvider()) {
            InstanceProvider<T> provider = key.getProvider();
            if (!providers.containsKey(key)) {
                providers.put(key, provider); // store for reset actions
            }
            return provider;
        } else {
            return (InstanceProvider<T>) providers.get(key);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(TypedKey<T> key) {
        T instance = (T) instances.get(key);
        if (instance == null) {
            InstanceProvider<T> provider = (InstanceProvider<T>) providers.get(key);
            if (provider == null) provider = key.getProvider();
            if (provider != null) instance = provider.get();
            if (instance == null && key.isPersistable()) { // see if available in prefs
                SharedPreferences prefs = getPrefs(key.isConfigType());
                String json = prefs.getString(key.getName(), null);
                instance = gson.fromJson(json, key.getValueClass());
            }
        }
        if (instance == null && key.getValueClass() == Boolean.class) {
            return (T) Boolean.FALSE; // default value for Boolean to avoid NPE for flags
        }
        return instance;
    }

    public static <T> T get(TypedKey<T> key, T defaultValue) {
        return has(key) ? get(key) : defaultValue;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static synchronized void reset() {
        Iterator<Map.Entry<TypedKey, Object>> iterator = instances.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<TypedKey, Object> entry = iterator.next();
            TypedKey key = entry.getKey();
            if (!key.isConfigType()) {
                try {
                    iterator.remove();
                } catch (Exception ignored) {}
            }
            if (key.hasProvider()) {
                key.getProvider().update(null); // clear all the values
            }
        }

        getPrefs(false).edit().clear().apply();

        for (Object provider : providers.values()) {
            ((InstanceProvider)provider).update(null); // clear all the values
        }
    }

    public static final class TestAccess {
        public static void clear() {
            instances.clear();
            providers.clear();
        }
    }
}
