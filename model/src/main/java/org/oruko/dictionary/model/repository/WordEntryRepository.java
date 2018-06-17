package org.oruko.dictionary.model.repository;

import org.oruko.dictionary.model.WordEntry;
import org.oruko.dictionary.model.State;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Transactional
public interface WordEntryRepository extends JpaRepository<WordEntry, Long> {

    /**
     * For finding a {@link WordEntry} given the word
     * @param word the word
     * @return {@link WordEntry}
     */
    WordEntry findByWord(String word);

    /**
     * For retrieving word entries by state. Supports pagination
     *
     * @param state the state of {@link WordEntry} to load
     * @param pageable the {@link Pageable} to represent pagination intent
     * @return list of {@link WordEntry}
     */
    List<WordEntry> findByState(State state, Pageable pageable);

    List<WordEntry> findByState(State state);
    Set<WordEntry> findByWordStartingWithAndState(String alphabet, State state);
    Set<WordEntry> findWordEntryByWordContainingAndState(String word, State state);
    Set<WordEntry> findWordEntryByVariantsContainingAndState(String word, State state);
    Set<WordEntry> findWordEntryByMeaningContainingAndState(String word, State state);
    WordEntry findByWordAndState(String word, State state);


    Integer countByState(State state);
    Boolean deleteByWordAndState(String word, State state);
    Boolean deleteByState(State state);
}
