/*
 *     Copyright 2019 REWOO Technologies AG
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.rewoo.elastic.util;

import org.junit.Test;

import javax.json.JsonObject;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;

public class JsonHelperTest {
    @Test
    public void createModificationObject() {
        final JsonObject modificationObject = JsonHelper.createModificationObject(42L, new Date(0), DateTimeFormatter.ISO_DATE_TIME);
        assertEquals("42", modificationObject.getString("userId"));
        assertEquals("1970-01-01T00:00:00Z[UTC]", modificationObject.getString("timestamp"));
        assertEquals("modification", modificationObject.getString("type"));
    }

    @Test
    public void getModificationTimestampRoundTrip() {
        final JsonObject modificationObject = JsonHelper.createModificationObject(42L, new Date(0), DateTimeFormatter.ISO_DATE_TIME);
        assertEquals(new Date(0), JsonHelper.getModificationTimestamp(modificationObject, DateTimeFormatter.ISO_DATE_TIME));
    }
}
