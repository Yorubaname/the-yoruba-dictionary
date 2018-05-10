package org.oruko.dictionary.web;

import org.oruko.dictionary.model.WordEntry;
import org.oruko.dictionary.model.State;
import org.oruko.dictionary.model.repository.WordEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service for managing name entries
 *
 * @author Hafiz Adewuyi.
 */
@Service
public class SuggestedNameService {
    private WordEntryRepository wordEntryRepository;

    /**
     * @param wordEntryRepository
     */
    @Autowired
    public SuggestedNameService(WordEntryRepository wordEntryRepository) {
        this.wordEntryRepository = wordEntryRepository;
    }


    /**
     * Saves {@link WordEntry}
     *
     * @param entry the entry to be saved
     */
    public WordEntry saveName(WordEntry entry) {
        entry.setState(State.SUGGESTED);
        return wordEntryRepository.save(entry);
    }

    /**
     * @return
     */
    public int countAll() {
        return wordEntryRepository.countByState(State.SUGGESTED);
    }

    public List<WordEntry> findAll() {
        return wordEntryRepository.findByState(State.SUGGESTED);
    }

    /**
     * Deletes the matching name entry if its current status is 'SUGGESTED'
     * @param id The primary key of the name entry to be deleted
     * @return A boolean indicating whether or not a deleted operation was actually executed
     */
    public boolean delete(Long id) {
        WordEntry suggestedName = wordEntryRepository.findOne(id);
        if (suggestedName != null && suggestedName.getState() == State.SUGGESTED) {
            wordEntryRepository.delete(suggestedName);
            return true;
        }
        return false;
    }

    public void deleteAll() {
        wordEntryRepository.deleteByState(State.SUGGESTED);
    }
}
