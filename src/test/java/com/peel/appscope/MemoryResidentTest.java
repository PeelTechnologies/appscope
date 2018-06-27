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
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@PrepareForTest({Context.class, SharedPreferences.class})
public class MemoryResidentTest {

    private Context context;
    private static final Gson gson = new Gson();

    private String keyGet;

    @Before
    public void setUp() {
        keyGet = null;
        context = AndroidFixtures.createMockContext(new AndroidFixtures.PrefsListener() {
            @Override public void onRemove(String key) {}
            @Override public void onPut(String key, Object value) {}
            @Override public void onGet(String key) {
                keyGet = key;
            }
        });
        AppScope.TestAccess.init(context, gson);
    }

    // no tests related to providers/memoryResident are needed as you can't construct
    // a key with memoryResident = false, but provider = true. This is because a provider
    // can't be bound for a key that is persistable.

    @Test
    public void testMemoryResidentWithPersistableKey() {
        TypedKey<String> key = new TypedKey.Builder<>("testKey", String.class)
                .persist()
                .build();
        AppScope.bind(key, "19999999999");
        AppScope.get(key);
        assertNull(keyGet); // get didn't access pref
    }

    @Test
    public void testNonMemoryResidentWithPersistableKey() {
        TypedKey<String> key = new TypedKey.Builder<>("testKey", String.class)
                .persist(false)
                .build();
        AppScope.bind(key, "19999999999");
        AppScope.get(key);
        assertEquals(key.getName(), keyGet); // get must access prefs
    }
}
