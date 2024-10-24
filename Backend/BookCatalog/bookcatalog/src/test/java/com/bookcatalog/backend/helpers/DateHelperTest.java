package com.newbookcatalog.newbookcatalog.helpers;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;

public class DateHelperTest {

     private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");

    @Test
    public void testClassLoading() {
        assertNotNull(new DateHelper());
    }

    @Test
    public void testSerialize_NullDate() {
        assertNull(DateHelper.serialize(null));
    }

    @Test
    public void testSerialize_ValidDate() {
        Date date = new Date();
        String expected = dateFormat.format(date);
        String actual = DateHelper.serialize(date);
        assertEquals(expected, actual);
    }

    @Test
    public void testDeserialize_NullString() throws IOException {
        assertNull(DateHelper.deserialize(null));
    }

    @Test
    public void testDeserialize_EmptyString() throws IOException {
        assertNull(DateHelper.deserialize(""));
    }

    @Test
    public void testDeserialize_ValidString() throws IOException, ParseException {
        String dateString = "12/2020";
        Date expected = dateFormat.parse(dateString);
        Date actual = DateHelper.deserialize(dateString);
        assertEquals(expected, actual);
    }

    @Test
    public void testDeserialize_InvalidString() {
        String dateString = "invalid date";
        IOException exception = assertThrows(IOException.class, () -> DateHelper.deserialize(dateString));
        assertEquals("Error parsing date", exception.getMessage());
    }
}
