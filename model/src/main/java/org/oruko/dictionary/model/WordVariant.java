package org.oruko.dictionary.model;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * Created by Hafiz on 5/11/2018.
 */
@Embeddable
public class WordVariant {
    @NotNull
    @NotEmpty
    private String word;

    @ManyToOne
    @JoinColumn(name = "geo_location_id")
    private GeoLocation geoLocation;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
