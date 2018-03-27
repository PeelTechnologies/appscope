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
 * Functional tests for {@link AppScope} treatment of persistent properties.
 *
 * @author Inderjeet Singh
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, SharedPreferences.class })
public class PersistentCacheFunctionalTest {

    private Context context;
    private static final Gson gson = new Gson();

    @Before
    public void setUp() {
    }

    @Test
    public void persistentPropertiesSavedEvenWhenEvictedInCache() {
        context = AndroidFixtures.createMockContext();
        AppScope.TestAccess.init(context, gson, "persistent_props", "config_props", 2);
        for (int i = 0; i < 20; ++i) {
            TypedKey<Boolean> key = new TypedKey<>("key" + i, Boolean.class, true, true);
            AppScope.bind(key, true);
        }
        // Even though we stored 20 persistent properties, only 2 remain in memory
        assertEquals(2, AppScope.TestAccess.getPersistentInMemoryCacheSize());
        // Even though only 2 are available in cache, all of them are restored from prefs
        for (int i = 0; i < 20; ++i) {
            TypedKey<Boolean> key = new TypedKey<>("key" + i, Boolean.class, true, true);
            assertTrue(AppScope.get(key));
        }
    }
}
