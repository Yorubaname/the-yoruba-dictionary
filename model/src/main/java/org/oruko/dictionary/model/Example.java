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
    public String englishTranslation;

    private ExampleType type;
}
