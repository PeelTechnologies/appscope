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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Unit tests for {@link AppScope}
 *
 * @author Inderjeet Singh
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { Context.class })
public class AppScopeTest {

    private Context context;
    private static final Gson gson = new Gson();

    @Before
    public void setUp() {
        context = Mockito.mock(Context.class);
        SharedPreferences persistPrefs = createMockSharedPreferences(context);
        SharedPreferences configPrefs = createMockSharedPreferences(context);
        Mockito.when(context.getSharedPreferences(AppScope.DEFAULT_PREFS_CLEAR_ON_RESET_FILE, Context.MODE_PRIVATE)).thenReturn(persistPrefs);
        Mockito.when(context.getSharedPreferences(AppScope.DEFAULT_PREFS_PERSIST_ON_RESET_FILE, Context.MODE_PRIVATE)).thenReturn(configPrefs);
        AppScope.TestAccess.reconfigure(context, gson);
    }

    @Test
    public void booleanDefaultValueOnGet() {
        TypedKey<Boolean> testKey = new TypedKey<>("testKey", Boolean.class, false, false);
        AppScope.remove(testKey);
        assertFalse(AppScope.get(testKey));
        AppScope.bind(testKey, true);
        assertTrue(AppScope.get(testKey));
        AppScope.remove(testKey);
    }

    @Test
    public void testBind() throws Exception {
        TypedKey<String> key = new TypedKey<>("userId", String.class, false, false);
        assertNull(AppScope.get(key));
        AppScope.bind(key, "19999999999");
        assertNotNull(AppScope.get(key));
        assertEquals("19999999999", AppScope.get(key));
    }

    @Test
    public void testBindIfNew() throws Exception {
        TypedKey<String> key = new TypedKey<>("userId", String.class, false, false);
        AppScope.bind(key, "19999999999");
        assertEquals("19999999999", AppScope.get(key));
        AppScope.bindIfAbsent(key, "16506953562");
        assertNotEquals("19999999999", "16506953562");
        assertNotEquals("16506953562", AppScope.get(key));
        assertEquals("19999999999", AppScope.get(key));
    }

    @Test
    public void testResetClearsPersistentProperties() throws Exception {
        TypedKey<String> persist = new TypedKey<>("persist", String.class, false, true);
        TypedKey<String> nonPersist = new TypedKey<>("nonPersist", String.class, false, false);
        AppScope.bind(persist, "a");
        AppScope.bind(nonPersist, "b");
        AppScope.reset();
        assertFalse(AppScope.has(persist));
        assertFalse(AppScope.has(nonPersist));
    }

    @Test
    public void testResetDoesntClearConfigProperties() throws Exception {
        TypedKey<String> configPersist = new TypedKey<>("configPersist", String.class, true, true);
        TypedKey<String> configNonPersist = new TypedKey<>("configNonPersist", String.class, true, false);
        TypedKey<String> nonConfigPersist = new TypedKey<>("nonConfigPersist", String.class, true, true);
        AppScope.bind(configPersist, "a");
        AppScope.bind(configNonPersist, "b");
        AppScope.reset();
        assertTrue(AppScope.has(configPersist));
        assertTrue(AppScope.has(configNonPersist));
        assertFalse(AppScope.has(nonConfigPersist));
    }

    @Test
    public void testTestAccessReconfigure() throws Exception {
        TypedKey<String> key = new TypedKey<>("userId", String.class, false, false);
        AppScope.bind(key, "a");
        assertEquals("a", AppScope.get(key));
        AppScope.TestAccess.reconfigure(context, gson);
        assertFalse(AppScope.has(key));
    }

    @Test
    public void testReconfigureClearsPersistentProperties() throws Exception {
        TypedKey<String> key = new TypedKey<>("key", String.class, false, true);
        AppScope.bind(key, "a");
        assertEquals("a", AppScope.get(key));
        AppScope.TestAccess.reconfigure(context, gson);
        assertFalse(AppScope.has(key));
    }

    @Test
    public void testReconfigureClearsConfigProperties() throws Exception {
        TypedKey<String> key = new TypedKey<>("userId", String.class, true, true);
        AppScope.bind(key, "a");
        assertEquals("a", AppScope.get(key));
        AppScope.TestAccess.reconfigure(context, gson);
        assertFalse(AppScope.has(key));
    }

    private SharedPreferences createMockSharedPreferences(Context context) {
        final Map<String, Object> map = new HashMap<>();
        return new SharedPreferences() {
            @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
            @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
            @SuppressWarnings("unchecked") private <T> T get(String key, T defValue) {
                return map.containsKey(key) ? (T) map.get(key) : defValue; 
            }
            @Override public Set<String> getStringSet(String key, Set<String> defValues) {
                return get(key, defValues);
            }
            @Override public String getString(String key, String defValue) {
                return get(key, defValue);
            }
            @Override public long getLong(String key, long defValue) {
                return get(key, defValue);
            }
            @Override public int getInt(String key, int defValue) {
                return get(key, defValue);
            }
            @Override public float getFloat(String key, float defValue) {
                return get(key, defValue);
            }
            @Override public boolean getBoolean(String key, boolean defValue) {
                return get(key, defValue);
            }
            @Override public Map<String, ?> getAll() {
                return map;
            }
            @Override public boolean contains(String key) {
                return map.containsKey(key);
            }
            @Override public Editor edit() {
                return new Editor() {
                    @Override public Editor remove(String key) {
                        map.remove(key);
                        return this;
                    }
                    @Override public Editor putStringSet(String key, Set<String> values) {
                        map.put(key, values);
                        return this;
                    }
                    @Override public Editor putString(String key, String value) {
                        map.put(key, value);
                        return this;
                    }
                    @Override public Editor putLong(String key, long value) {
                        map.put(key, value);
                        return this;
                    }
                    @Override public Editor putInt(String key, int value) {
                        map.put(key, value);
                        return this;
                    }
                    @Override public Editor putFloat(String key, float value) {
                        map.put(key, value);
                        return this;
                    }
                    @Override public Editor putBoolean(String key, boolean value) {
                        map.put(key, value);
                        return this;
                    }
                    @Override public boolean commit() {
                        return true;
                    }
                    @Override public Editor clear() {
                        map.clear();
                        return this;
                    }
                    @Override public void apply() {} // no op
                };
            }
        };
    }
}
