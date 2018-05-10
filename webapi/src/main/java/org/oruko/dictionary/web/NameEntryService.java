package org.oruko.dictionary.web;

import org.oruko.dictionary.model.WordEntry;
import org.oruko.dictionary.model.WordEntryFeedback;
import org.oruko.dictionary.model.State;
import org.oruko.dictionary.model.exception.RepositoryAccessError;
import org.oruko.dictionary.model.repository.WordEntryFeedbackRepository;
import org.oruko.dictionary.model.repository.WordEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The service for managing name entries
 *
 * @author Dadepo Aderemi.
 */
@Service
public class NameEntryService {

    private Integer BATCH_SIZE = 50;
    private Integer PAGE = 0;
    private Integer COUNT_SIZE = 50;

    private WordEntryRepository wordEntryRepository;
    private WordEntryFeedbackRepository wordEntryFeedbackRepository;

    /**
     * Public constructor for {@link NameEntryService} depends on instances of
     *
     * @param wordEntryRepository         Repository responsible for persisting {@link WordEntry}
     * @param wordEntryFeedbackRepository Repository responsible for persisting {@link WordEntryFeedback}
     */
    @Autowired
    public NameEntryService(WordEntryRepository wordEntryRepository,
                            WordEntryFeedbackRepository wordEntryFeedbackRepository) {
        this.wordEntryRepository = wordEntryRepository;
        this.wordEntryFeedbackRepository = wordEntryFeedbackRepository;
    }

    /**
     * Adds a new name if not present. If already present, adds the name to the
     * duplicate table.
     *
     * @param entry
     */
    public void insertTakingCareOfDuplicates(WordEntry entry) {
        String name = entry.getName();

        if (namePresentAsVariant(name)) {
            throw new RepositoryAccessError("Given name already exists as a variant entry");
        }

        if (alreadyExists(name)) {
            throw new RepositoryAccessError("Given name already exists in the index");
        }

        wordEntryRepository.save(entry);
    }


    /**
     * Adds a list of names in bulk if not present. If any of the name is already present, adds the name to the
     * duplicate table.
     *
     * @param entries the list of names
     */
    public void bulkInsertTakingCareOfDuplicates(List<WordEntry> entries) {
        int i = 0;
        for (WordEntry entry : entries) {
            this.insertTakingCareOfDuplicates(entry);
            i++;

            if (i == BATCH_SIZE) {
                wordEntryRepository.flush();
                i = 0;
            }
        }
    }


    /**
     * Returns all the feedback for a name, sorted by time submitted
     *
     * @return the feedback as a list of {@link WordEntryFeedback}
     */
    public List<WordEntryFeedback> getFeedback(WordEntry entry) {
        final Sort sort = new Sort(Sort.Direction.DESC, "submittedAt");
        return wordEntryFeedbackRepository.findByName(entry.getName(), sort);
    }

    /**
     * Saves {@link WordEntry}
     *
     * @param entry the entry to be saved
     */
    public WordEntry saveName(WordEntry entry) {
        return wordEntryRepository.save(entry);
    }

    /**
     * Saves a list {@link WordEntry}
     *
     * @param entries the list of name entries to be saved
     */
    public List<WordEntry> saveNames(List<WordEntry> entries) {
        int i = 0;
        List<WordEntry> savedNames = new ArrayList<>();
        for (WordEntry entry : entries) {
            savedNames.add(this.saveName(entry));
            i++;
            if (i == BATCH_SIZE) {
                wordEntryRepository.flush();
                i = 0;
            }
        }
        return savedNames;
    }


    /**
     * /**
     * Updates the properties with values from another {@link WordEntry}
     *
     * @param oldEntry the entry to be updated
     * @param newEntry the entry with the new value
     * @return the updated entry
     */
    public WordEntry updateName(WordEntry oldEntry, WordEntry newEntry) {
        String oldEntryName = oldEntry.getName();
        // update main entry
        oldEntry.update(newEntry);
        return wordEntryRepository.save(oldEntry);
    }


    /**
     * Updates the properties of a list of names with values from another list of name entries
     *
     * @param nameEntries the new entries
     * @return the updated entries
     */
    public List<WordEntry> bulkUpdateNames(List<WordEntry> nameEntries) {
        List<WordEntry> updated = new ArrayList<>();

        int i = 0;
        for (WordEntry wordEntry : nameEntries) {
            WordEntry oldEntry = this.loadName(wordEntry.getName());
            updated.add(this.updateName(oldEntry, wordEntry));
            i++;

            if (i == BATCH_SIZE) {
                wordEntryRepository.flush();
                i = 0;
            }
        }
        return updated;
    }

    /**
     * Used to retrieve {@link WordEntry} from the repository. Supports ability to
     * specify how many to retrieve and pagination.
     *
     * @param pageNumberParam specifies page number
     * @param countParam      specifies the count of result
     * @return a list of {@link WordEntry}
     */
    public List<WordEntry> loadAllNames(Optional<Integer> pageNumberParam, Optional<Integer> countParam) {

        List<WordEntry> nameEntries = new ArrayList<>();
        Integer pageNumber = pageNumberParam.orElse(PAGE);
        Integer count = countParam.orElse(COUNT_SIZE);

        PageRequest request =
                new PageRequest(pageNumber == 0 ? 0 : pageNumber - 1, count, Sort.Direction.ASC, "id");

        Page<WordEntry> pages = wordEntryRepository.findAll(request);
        pages.forEach(nameEntries::add);

        return nameEntries;
    }

    /**
     * Used to retrieve {@link WordEntry} of given state from the repository.
     *
     * @param state the {@link State} of the entry
     * @return list of {@link WordEntry}. If state is not present, it returns an empty list
     */
    public List<WordEntry> loadAllByState(Optional<State> state) {
        return state.map(s -> wordEntryRepository.findByState(s)).orElseGet(Collections::emptyList);
    }

    /**
     * Used to retrieve paginated result of {@link WordEntry} of given state from the repository
     *
     * @param state      state the {@link State} of the entry
     * @param pageParam  specifies page number
     * @param countParam specifies the count of result
     * @return a list of {@link WordEntry}
     */
    public List<WordEntry> loadByState(Optional<State> state, Optional<Integer> pageParam, Optional<Integer> countParam) {

        if (!state.isPresent()) {
            return this.loadAllNames(pageParam, countParam);
        }

        final Integer page = pageParam.map(integer -> integer - 1).orElse(1);
        final Integer count = countParam.orElse(COUNT_SIZE);

        return wordEntryRepository.findByState(state.get(), new PageRequest(page, count));

    }

    /**
     * Used to retrieve all {@link WordEntry} from the repository.
     *
     * @return a list of all {@link WordEntry}
     */
    public List<WordEntry> loadAllNames() {
        return wordEntryRepository.findAll();
    }

    /**
     * Returns the number of names in the database
     *
     * @return number of names
     */
    public Long getNameCount() {
        return wordEntryRepository.count();
    }

    /**
     * Used to retrieve a {@link WordEntry} from the repository using its known name
     *
     * @param name the name
     * @return the WordEntry
     */
    public WordEntry loadName(String name) {
        return wordEntryRepository.findByName(name);
    }

    /**
     * Duplicates all the name entry plus the duplicates
     */
    public void deleteAllAndDuplicates() {
        wordEntryRepository.deleteAll();
        //TODO introduce an event that all names have been deleted
    }


    /**
     * Duplicates a name entry plus its duplicates
     *
     * @param name the name to delete
     */
    public void deleteNameEntryAndDuplicates(String name) {
        WordEntry wordEntry = wordEntryRepository.findByName(name);
        wordEntryRepository.delete(wordEntry);
    }

    /**
     * Deletes multiple name entries and their duplicates
     *
     * @param names a list of names to delete their entries and their duplicates
     */
    public void batchDeleteNameEntryAndDuplicates(List<String> names) {
        int i = 0;
        for (String name : names) {
            this.deleteNameEntryAndDuplicates(name);
            i++;

            if (i == BATCH_SIZE) {
                wordEntryRepository.flush();
                i = 0;
            }
        }
    }

    // ==================================================== Helpers ====================================================
    private boolean alreadyExists(String name) {
        WordEntry entry = wordEntryRepository.findByName(name);
        return entry != null;
    }

    private boolean namePresentAsVariant(String name) {
        // TODO revisit. Might end up being impacting performance
        List<WordEntry> allNames = wordEntryRepository.findAll();
        return allNames.stream().anyMatch((nameEntry) -> {
            if (nameEntry.getVariants() != null) {
                return nameEntry.getVariants().contains(name);
            }
            return false;
        });
    }
}
