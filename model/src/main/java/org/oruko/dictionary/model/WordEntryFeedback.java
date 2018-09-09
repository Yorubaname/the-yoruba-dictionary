package org.oruko.dictionary.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entity representing the feedback given for a word
 *
 * Created by Dadepo Aderemi.
 */
@Entity
public class WordEntryFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column
    private String word;
    @Column(length = 2000)
    private String feedback;
    @Column
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    private LocalDateTime submittedAt;

    /** no args constructor for JPA **/
    public WordEntryFeedback() {
    }

    public WordEntryFeedback(@JsonProperty("word") String word,
                             @JsonProperty("feedback") String feedback) {
        this.word = word;
        this.feedback = feedback;
        this.submittedAt = LocalDateTime.now();
    }


    public long getId() {
        return id;
    }
    public String getWord() {
        return word;
    }
    public String getFeedback() {
        return feedback;
    }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
