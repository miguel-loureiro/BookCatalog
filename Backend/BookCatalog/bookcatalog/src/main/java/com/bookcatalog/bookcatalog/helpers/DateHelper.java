package com.bookcatalog.bookcatalog.helpers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");

    public static String serialize(Date date) {
        if (date == null) {
            return null;
        }
        return dateFormat.format(date);
    }

    public static Date deserialize (String dateString) throws IOException {

        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException error) {
            throw new IOException("Error parsing date", error);
        }
    }
}
