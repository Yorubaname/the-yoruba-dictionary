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
     * For finding a {@link WordEntry} given the name
     * @param name the name
     * @return {@link WordEntry}
     */
    WordEntry findByName(String name);

    /**
     * For retrieving name entries by state. Supports pagenation
     *
     * @param state the state of {@link WordEntry} to load
     * @param pageable the {@link Pageable} to represent pagination intent
     * @return list of {@link WordEntry}
     */
    List<WordEntry> findByState(State state, Pageable pageable);

    /**
     * For retrieving all name entries by state
     * @param state the state of {@link WordEntry} to load
     * @return list of {@link WordEntry}
     */
    List<WordEntry> findByState(State state);
    Set<WordEntry> findByNameStartingWithAndState(String alphabet, State state);
    Set<WordEntry> findNameEntryByNameContainingAndState(String name, State state);
    Set<WordEntry> findNameEntryByVariantsContainingAndState(String name, State state);
    Set<WordEntry> findNameEntryByMeaningContainingAndState(String name, State state);
    Set<WordEntry> findNameEntryByExtendedMeaningContainingAndState(String name, State state);
    WordEntry findByNameAndState(String name, State state);


    Integer countByState(State state);
    Boolean deleteByNameAndState(String name, State state);
    Boolean deleteByState(State state);
}
