package org.oruko.dictionary.web;

import org.oruko.dictionary.model.NameEntry;
import org.oruko.dictionary.model.State;
import org.oruko.dictionary.model.repository.NameEntryRepository;
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
    private NameEntryRepository nameEntryRepository;

    /**
     * @param nameEntryRepository
     */
    @Autowired
    public SuggestedNameService(NameEntryRepository nameEntryRepository) {
        this.nameEntryRepository = nameEntryRepository;
    }


    /**
     * Saves {@link org.oruko.dictionary.model.NameEntry}
     *
     * @param entry the entry to be saved
     */
    public NameEntry saveName(NameEntry entry) {
        entry.setState(State.SUGGESTED);
        return nameEntryRepository.save(entry);
    }

    /**
     * @return
     */
    public int countAll() {
        return nameEntryRepository.countByState(State.SUGGESTED);
    }

    public List<NameEntry> findAll() {
        return nameEntryRepository.findByState(State.SUGGESTED);
    }

    /**
     * Deletes the matching name entry if its current status is 'SUGGESTED'
     * @param id The primary key of the name entry to be deleted
     * @return A boolean indicating whether or not a deleted operation was actually executed
     */
    public boolean delete(Long id) {
        NameEntry suggestedName = nameEntryRepository.findOne(id);
        if (suggestedName != null && suggestedName.getState() == State.SUGGESTED) {
            nameEntryRepository.delete(suggestedName);
            return true;
        }
        return false;
    }

    public void deleteAll() {
        nameEntryRepository.deleteByState(State.SUGGESTED);
    }
}
