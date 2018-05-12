package org.oruko.dictionary.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Hafiz on 5/11/2018.
 */
@Entity
public class Definition {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime submittedAt;

    @Column(length = 5000)
    @NotNull
    @NotEmpty
    private String content;

    @Column(length = 5000)
    public String englishTranslation;

    @ElementCollection
    private List<Example> examples;

    @ManyToOne(fetch=FetchType.LAZY, targetEntity = WordEntry.class)
    @JoinColumn(name="word_id")
    private AbstractWordEntry owner;

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEnglishTranslation() {
        return englishTranslation;
    }

    public void setEnglishTranslation(String englishTranslation) {
        this.englishTranslation = englishTranslation;
    }

    public List<Example> getExamples() {
        return examples;
    }

    public void setExamples(List<Example> examples) {
        this.examples = examples;
    }

    public long getId() {
        return id;
    }
}
