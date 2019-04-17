package org.oruko.dictionary.website.handlebarshelpers;
import pl.allegro.tech.boot.autoconfigure.handlebars.HandlebarsHelper;

@HandlebarsHelper
public class StringHelper {
    public StringHelper() {
    }

    public static CharSequence toUpperCase(String string) {
        return string.toUpperCase();
    }

    public static CharSequence toLowerCase(String string) {
        return string == null ? null : string.toLowerCase();
    }
}
