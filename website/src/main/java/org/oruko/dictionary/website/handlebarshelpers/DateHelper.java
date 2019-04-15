package org.oruko.dictionary.website.handlebarshelpers;
import pl.allegro.tech.boot.autoconfigure.handlebars.HandlebarsHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@HandlebarsHelper
public class DateHelper {
    public DateHelper() {
    }

    public static CharSequence longDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        String formatDateTime = dateTime.format(formatter);
        return formatDateTime;
    }
}
