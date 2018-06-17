package org.oruko.dictionary.model;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * Created by Hafiz on 5/11/2018.
 */
@Embeddable
public class Example {
    @NotNull
    @NotEmpty
    @Column(length = 5000)
    private String content;

    @Column(length = 5000)
    private String englishTranslation;

    private ExampleType type;

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

    public ExampleType getType() {
        return type;
    }

    public void setType(ExampleType type) {
        this.type = type;
    }
}
