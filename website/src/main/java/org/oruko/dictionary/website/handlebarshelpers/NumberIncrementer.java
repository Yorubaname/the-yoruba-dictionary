package org.oruko.dictionary.website.handlebarshelpers;
import pl.allegro.tech.boot.autoconfigure.handlebars.HandlebarsHelper;

@HandlebarsHelper
public class NumberIncrementer {
    public NumberIncrementer() {
    }

    public static CharSequence increment(Integer integer) {
        integer += 1;
        return integer.toString();
    }
}
