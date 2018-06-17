package org.oruko.dictionary.model;


import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Entity for persisting WordDto entries
 */
@Entity
@Table(name = "word_entry")
public class WordEntry extends AbstractWordEntry implements Comparable<WordEntry> {

    @Column(unique = true)
    @NotNull
    @NotEmpty
    private String word;

    public WordEntry() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public WordEntry(String word) {
        this.word = word;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Returns the identifier, in this case the database primary key
     *
     * @return the identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Get the word
     *
     * @return returns the word
     */
    public String getWord() {
        return word;
    }

    /**
     * Set the word
     *
     * @param word the word
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * Updates properties using another instance of {@link WordEntry}
     * The state of the update word entry will be changed to modified
     */
    public void update(WordEntry wordEntry) {
        BeanUtils.copyProperties(wordEntry, this, "definitions");
        // TODO revisit how to get this done on the entity level: how to get @Temporary working with LocalDateTime
        if (State.PUBLISHED.equals(this.getState())) {
            this.setState(State.MODIFIED);
        }
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Implements the sorting algorithm for {@link WordEntry}
     *
     * @param nameToCompare the instance of {@link WordEntry} to
     *                      compare with.
     * @return -1, 0 or 1.
     */
    @Override
    public int compareTo(WordEntry nameToCompare) {
        return this.word.compareTo(nameToCompare.getWord());
    }
}
