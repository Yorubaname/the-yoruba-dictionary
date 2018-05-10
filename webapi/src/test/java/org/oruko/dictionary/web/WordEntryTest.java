package org.oruko.dictionary.web;

import org.junit.*;
import org.oruko.dictionary.model.GeoLocation;
import org.oruko.dictionary.model.WordEntry;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class WordEntryTest {

    @Test
    public void testUpdate() throws Exception {
        GeoLocation geoLocation = mock(GeoLocation.class);
        WordEntry wordEntry = new WordEntry("Ajani");
        wordEntry.setGeoLocation(Collections.singletonList(geoLocation));
        wordEntry.setMeaning("meaning");
        wordEntry.setMorphology("Morphology");
        wordEntry.setSubmittedBy("submittedBy");
        wordEntry.setTonalMark(new char[]{'A'});

        WordEntry newEntry = new WordEntry("Ajani");
        newEntry.setGeoLocation(Collections.singletonList(geoLocation));
        newEntry.setMeaning("meaning1");
        newEntry.setMorphology("morphology1");
        newEntry.setSubmittedBy("submittedBy1");
        char[] tonalMark = {'B'};
        newEntry.setTonalMark(tonalMark);

        // System under test
        wordEntry.update(newEntry);

        assertEquals("Ajani", wordEntry.getName());
        assertEquals(Collections.singletonList(geoLocation), wordEntry.getGeoLocation());
        assertEquals("morphology1", wordEntry.getMorphology());
        assertEquals("submittedBy1", wordEntry.getSubmittedBy());
        assertEquals(tonalMark, wordEntry.getTonalMark());
    }
}