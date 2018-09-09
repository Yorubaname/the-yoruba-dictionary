package org.oruko.dictionary.web.event;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.oruko.dictionary.events.WordSearchedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles {@link WordSearchedEvent}
 * Created by Dadepo Aderemi.
 */
@Component
public class NameSearchedEventHandler {


    private RecentSearches recentSearches;

    @Autowired
    public NameSearchedEventHandler(RecentSearches recentSearches) {
        this.recentSearches = recentSearches;
    }

    @Subscribe
    @AllowConcurrentEvents
    public void listen(WordSearchedEvent event) {
        // Handle when a name is searched
        try {
            recentSearches.stack(event.getNameSearched());
        } catch (Exception e) {
            //TODO log this
        }
    }
}
