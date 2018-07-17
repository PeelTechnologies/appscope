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

import static com.peel.appscope.AppScope.NON_PERSISTENT;
import static com.peel.appscope.AppScope.SURVIVE_RESET;
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
import com.peel.prefs.TypedKey;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Functional tests for reset related use-cases for {@link AppScope}
 *
 * @author Inderjeet Singh
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, SharedPreferences.class, PreferenceManager.class})
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
        TypedKey<String> persist = new TypedKey<>("persist", String.class);
        TypedKey<String> nonPersist = new TypedKey<>("nonPersist", String.class, NON_PERSISTENT);
        AppScope.put(persist, "a");
        AppScope.put(nonPersist, "b");
        AppScope.reset();
        assertFalse(AppScope.contains(persist));
        assertFalse(AppScope.contains(nonPersist));
    }

    @Test
    public void testResetDoesntClearsConfigProviderProperties() throws Exception {
        TypedKeyWithProvider<String> key1 = new TypedKeyWithProvider<String>("key1", String.class,
                new StringProvider("a"), NON_PERSISTENT);
        TypedKeyWithProvider<String> key2 = new TypedKeyWithProvider<String>("key2", String.class,
                new StringProvider("b"), SURVIVE_RESET, NON_PERSISTENT) ;
        AppScope.register(key1);
        AppScope.register(key2);

        // AppScope.reset() shouldn't clear config provider properties
        AppScope.reset();
        assertNull(AppScope.get(key1));
        assertEquals("b", AppScope.get(key2));

        // TestAccess.init should clear all properties
        key1.getProvider().update("c");
        assertEquals("c", AppScope.get(key1));
        key2.getProvider().update("d");
        assertEquals("d", AppScope.get(key2));
        AppScope.TestAccess.reset();
        assertNull(AppScope.get(key1));
        assertNull(AppScope.get(key2));
    }

    @Test
    public void testResetIgnoresBrokenProviders() throws Exception {
        TypedKeyWithProvider<String> key1 = new TypedKeyWithProvider<String>("key1", String.class, new InstanceProvider<String>() {
            private String value = "a";
            @Override public String get() {
                return value;
            }
            @Override public void update(String value) {
                throw new IllegalStateException();
            }
        }, NON_PERSISTENT);
        AppScope.register(key1);
        TypedKey<String> key2 = new TypedKey<>("key2", String.class, NON_PERSISTENT);
        AppScope.put(key2, "b");
        AppScope.TestAccess.init(context, gson);

        assertEquals("a", AppScope.get(key1));
        assertNull(AppScope.get(key2));
    }

    @Test
    public void testResetDoesntClearConfigProperties() throws Exception {
        TypedKey<String> configPersist = new TypedKey<>("configPersist", String.class, SURVIVE_RESET);
        TypedKey<String> configNonPersist = new TypedKey<>("configNonPersist", String.class, SURVIVE_RESET, NON_PERSISTENT);
        TypedKey<String> nonConfigPersist = new TypedKey<>("nonConfigPersist", String.class, SURVIVE_RESET);
        AppScope.put(configPersist, "a");
        AppScope.put(configNonPersist, "b");
        AppScope.reset();
        assertTrue(AppScope.contains(configPersist));
        assertTrue(AppScope.contains(configNonPersist));
        assertFalse(AppScope.contains(nonConfigPersist));
    }

    @Test
    public void testResetUpdatesProviderAsWellForABoundInstance() throws Exception {
        TypedKeyWithProvider<String> country = new TypedKeyWithProvider<String>("country", String.class,
                new InstanceProvider<String>() {
            private static final String defaultCountry = "US";
            private String value = defaultCountry;
            @Override public void update(String country) {
                this.value = country == null ? defaultCountry : country;
            }
            @Override public String get() {
                return value;
            }
        });

        String defaultCountryCode = AppScope.get(country);
        AppScope.put(country, "FR");
        assertEquals("FR", AppScope.get(country));
        AppScope.reset();

        assertEquals(defaultCountryCode, AppScope.get(country));
        AppScope.put(country, "IN");
        assertEquals("IN", AppScope.get(country));
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
