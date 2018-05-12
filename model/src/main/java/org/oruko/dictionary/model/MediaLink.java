package org.oruko.dictionary.model;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * Entity representing link to media (video/audio) serving as explanation for the owning word
 * Created by Hafiz on 5/11/2018.
 */
@Embeddable
public class MediaLink {
    @NotNull
    @NotEmpty
    private String link;
    private String caption;
    private  MediaType type;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }
}
