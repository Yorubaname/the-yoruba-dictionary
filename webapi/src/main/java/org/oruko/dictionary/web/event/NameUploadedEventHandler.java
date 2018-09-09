package org.oruko.dictionary.web.event;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.oruko.dictionary.events.WordUploadedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles {@link WordUploadedEvent}
 * Created by Dadepo Aderemi.
 */
@Component
public class NameUploadedEventHandler {

    private WordUploadStatus uploadStatus;

    @Autowired
    public NameUploadedEventHandler(WordUploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    @Subscribe
    @AllowConcurrentEvents
    public void listen(WordUploadedEvent event) {
        // Handle when a name is searched
        try {
            uploadStatus.setStatus(event);
        } catch (Exception e) {
            //TODO log this
        }
    }
}
