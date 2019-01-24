package com.rewoo.elastic.util;

import org.junit.Test;

import javax.json.JsonObject;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;

public class JsonHelperTest {
    @Test
    public void createModificationObject() {
        JsonObject modificationObject = JsonHelper.createModificationObject(42L, new Date(0), DateTimeFormatter.ISO_DATE_TIME);
        assertEquals("42", modificationObject.getString("userId"));
        assertEquals("1970-01-01T00:00:00Z[UTC]", modificationObject.getString("timestamp"));
        assertEquals("modification", modificationObject.getString("type"));
    }

    @Test
    public void getModificationTimestampRoundTrip() {
        JsonObject modificationObject = JsonHelper.createModificationObject(42L, new Date(0), DateTimeFormatter.ISO_DATE_TIME);
        assertEquals(new Date(0), JsonHelper.getModificationTimestamp(modificationObject, DateTimeFormatter.ISO_DATE_TIME));
    }
}
