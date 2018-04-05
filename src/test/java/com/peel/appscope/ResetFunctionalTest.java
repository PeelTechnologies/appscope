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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Functional tests for reset related use-cases for {@link AppScope}
 *
 * @author Inderjeet Singh
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, SharedPreferences.class })
public class ResetFunctionalTest {

    private Context context;
    private static final Gson gson = new Gson();

    @Before
    public void setUp() {
        context = AndroidFixtures.createMockContext();
        AppScope.TestAccess.init(context, gson);
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
    public void testResetDoesntClearsConfigProviderProperties() throws Exception {
        TypedKey<String> key1 = new TypedKey<>("key1", String.class, false, false);
        TypedKey<String> key2 = new TypedKey<>("key2", String.class, true, false);
        AppScope.bindProvider(key1, new StringProvider("a"));
        AppScope.bindProvider(key2, new StringProvider("b"));

        // AppScope.reset() shouldn't clear config provider properties
        AppScope.reset();
        assertNull(AppScope.get(key1));
        assertEquals("b", AppScope.get(key2));

        // TestAccess.init should clear all properties
        AppScope.bindProvider(key1, new StringProvider("c"));
        AppScope.bindProvider(key2, new StringProvider("d"));
        AppScope.TestAccess.reset();
        assertNull(AppScope.get(key1));
        assertNull(AppScope.get(key2));
    }

    @Test
    public void testResetIgnoresBrokenProviders() throws Exception {
        TypedKey<String> key1 = new TypedKey<>("key1", String.class, false, false);
        AppScope.bindProvider(key1, new InstanceProvider<String>() {
            private String value = "a";
            @Override public String get() {
                return value;
            }
            @Override public void update(String value) {
                throw new IllegalStateException();
            }
        });
        TypedKey<String> key2 = new TypedKey<>("key2", String.class, false, false);
        AppScope.bind(key2, "b");
        AppScope.TestAccess.init(context, gson);

        assertNull(AppScope.get(key1));
        assertNull(AppScope.get(key2));
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

    private static final class StringProvider implements InstanceProvider<String> {
        private String value;

        public StringProvider(String value) {
            this.value = value;
        }

        @Override
        public void update(String value) {
            this.value = value;
        }

        @Override
        public String get() {
            return value;
        }
    }
}
