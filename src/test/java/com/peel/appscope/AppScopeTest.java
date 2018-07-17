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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.Gson;
import com.peel.prefs.TypedKey;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Unit tests for {@link AppScope}
 *
 * @author Inderjeet Singh
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, SharedPreferences.class, PreferenceManager.class})
public class AppScopeTest {

    private Context context;
    private static final Gson gson = new Gson();
    private String keyGet;
    private String keyPut;

    @Before
    public void setUp() {
        keyGet = null;
        context = AndroidFixtures.createMockContext(new AndroidFixtures.PrefsListener() {
            @Override public void onGet(String key) {
                keyGet = key;
            }
            @Override public void onPut(String key, Object value) {
                keyPut = key;
            }
        });
        AppScope.TestAccess.init(context, gson);
    }

    @Test
    public void booleanDefaultValueOnGet() {
        TypedKey<Boolean> testKey = new TypedKey<>("testKey", Boolean.class, AppScope.NON_PERSISTENT);
        AppScope.remove(testKey);
        assertFalse(AppScope.get(testKey));
        AppScope.put(testKey, true);
        assertTrue(AppScope.get(testKey));
        AppScope.remove(testKey);
    }

    @Test
    public void testPut() throws Exception {
        TypedKey<String> key = new TypedKey<>("userId", String.class, AppScope.NON_PERSISTENT);
        assertNull(AppScope.get(key));
        AppScope.put(key, "19999999999");
        assertNotNull(AppScope.get(key));
        assertEquals("19999999999", AppScope.get(key));
    }

    @Test
    public void testPutWithKeyAndClass() throws Exception {
        assertNull(AppScope.get("k2", String.class));
        AppScope.put("k2", String.class, "19999999999");
        assertEquals("k2", keyPut);
        assertEquals("19999999999", AppScope.get("k2", String.class));
        assertEquals("k2", keyGet);
    }

    @Test
    public void testRegisterProvider() throws Exception {
        TypedKeyWithProvider<String> key = new TypedKeyWithProvider<String>("key", String.class,
                new InstanceProvider<String>() {
            private String value = "a";
            @Override public void update(String value) {
                this.value = value;
            }
            @Override public String get() {
                return value;
            }
        });
        AppScope.register(key);
        assertEquals("a", AppScope.get(key));
        AppScope.put(key, "b");
        assertEquals("b", AppScope.get(key));
    }

    @Test
    public void testTestAccessReconfigure() throws Exception {
        TypedKey<String> key = new TypedKey<>("userId", String.class, AppScope.NON_PERSISTENT);
        AppScope.put(key, "a");
        assertEquals("a", AppScope.get(key));
        AppScope.TestAccess.init(context, gson);
        assertFalse(AppScope.contains(key));
    }

    @Test
    public void testReconfigureClearsPersistentProperties() throws Exception {
        TypedKey<String> key = new TypedKey<>("key", String.class);
        AppScope.put(key, "a");
        assertEquals("a", AppScope.get(key));
        AppScope.TestAccess.init(context, gson);
        assertFalse(AppScope.contains(key));
    }

    @Test
    public void testReconfigureClearsConfigProperties() throws Exception {
        TypedKey<String> key = new TypedKey<>("userId", String.class, AppScope.SURVIVE_RESET);
        AppScope.put(key, "a");
        assertEquals("a", AppScope.get(key));
        AppScope.TestAccess.init(context, gson);
        assertFalse(AppScope.contains(key));
    }
}
