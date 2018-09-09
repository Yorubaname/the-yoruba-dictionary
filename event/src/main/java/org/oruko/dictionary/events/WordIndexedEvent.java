package org.oruko.dictionary.events;

import java.time.LocalDateTime;

/**
 * Event when a name has been indexed
 * Created by Dadepo Aderemi.
 */
public class WordIndexedEvent {
    private final String name;
    private final LocalDateTime timestamp;

    public WordIndexedEvent(String name) {
        this.name = name;
        this.timestamp = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
