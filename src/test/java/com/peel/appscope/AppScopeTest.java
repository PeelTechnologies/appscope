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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for {@link AppScope}
 *
 * @author Inderjeet Singh
 */
public class AppScopeTest {

    @Test
    public void booleanDefaultValueOnGet() {
        TypedKey<Boolean> testKey = new TypedKey<>("testKey",
                Boolean.class, false, false);
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
}
