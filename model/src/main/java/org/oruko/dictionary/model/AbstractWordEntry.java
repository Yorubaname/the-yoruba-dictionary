package org.oruko.dictionary.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Parent abstract class shared by {@link WordEntry}
 *
 * @author Dadepo Aderemi.
 */
@MappedSuperclass
//TODO revisit the entries and use a more appropriate data type in cases this is necessary
public abstract class AbstractWordEntry {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Column
    protected String pronunciation;

    @Column
    protected String partOfSpeech;

    @Column
    protected String style;

    @Column
    protected  String grammaticalFeature;

    @Column
    protected String ipaNotation;

    @ElementCollection
    protected List<WordVariant> variants;

    @Column
    protected String syllables;

    // TODO: Remove this field from this entity
    @Column(length = 5000)
    protected String meaning;

    @Column(length = 1000)
    protected String morphology;

    @JoinColumn(name = "geo_location_id")
    @ManyToMany
    protected List<GeoLocation> geoLocation;

    @Column(length = 1000)
    protected String famousPeople;

    @Column(length = 1000)
    protected String inOtherLanguages;

    @Column(length = 1000)
    protected String media;

    @Column
    protected char[] tonalMark;

    @Column(length = 1000)
    protected String tags;

    @Column(length = 1000)
    protected String submittedBy = "Not Available";

    @ElementCollection
    protected List<Etymology> etymology;

    @ElementCollection
    protected List<MediaLink> mediaLinks;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "word_id")
    protected List<Definition> definitions;

    @Column
    @Enumerated(EnumType.STRING)
    protected State state;

    @Column
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    protected LocalDateTime createdAt;

    @Column
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    protected LocalDateTime updatedAt;

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    /**
     * get the tonal mark
     * @return the tonal mark
     */
    public char[] getTonalMark() {
        return tonalMark;
    }

    /**
     * Set the tonal mark
     * @param tonalMark the total mark
     */
    public void setTonalMark(char[] tonalMark) {
        this.tonalMark = tonalMark;
    }

    /**
     * Get the meaning
     * @return the meaning
     */
    public String getMeaning() {
        return meaning;
    }

    /**
     * Sets the meaning
     * @param meaning the meaning
     */
    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    /**
     * Get the geo location
     * @return the geo location
     */
    public List<GeoLocation> getGeoLocation() {
        return geoLocation;
    }

    /**
     * Sets the geo location
     * @param geoLocation the geo location
     */
    public void setGeoLocation(List<GeoLocation> geoLocation) {
        this.geoLocation = geoLocation;
    }

    public List<Etymology> getEtymology() {
        return etymology;
    }

    public void setEtymology(List<Etymology> etymology) {
        this.etymology = etymology;
    }

    public String getMorphology() {
        return morphology;
    }

    public void setMorphology(String morphology) {
        this.morphology = morphology;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getIpaNotation() {
        return ipaNotation;
    }

    public void setIpaNotation(String ipaNotation) {
        this.ipaNotation = ipaNotation;
    }

    public String getSyllables() {
        return syllables;
    }

    public void setSyllables(String syllables) {
        this.syllables = syllables;
    }

    public List<WordVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<WordVariant> variants) {
        this.variants = variants;
    }

    public String getFamousPeople() {
        return famousPeople;
    }

    public void setFamousPeople(String famousPeople) {
        this.famousPeople = famousPeople;
    }

    public String getInOtherLanguages() {
        return inOtherLanguages;
    }

    public void setInOtherLanguages(String inOtherLanguages) {
        this.inOtherLanguages = inOtherLanguages;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<MediaLink> getMediaLinks() {
        return mediaLinks;
    }

    public void setMediaLinks(List<MediaLink> mediaLinks) {
        this.mediaLinks = mediaLinks;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getGrammaticalFeature() {
        return grammaticalFeature;
    }

    public void setGrammaticalFeature(String grammaticalFeature) {
        this.grammaticalFeature = grammaticalFeature;
    }
}
