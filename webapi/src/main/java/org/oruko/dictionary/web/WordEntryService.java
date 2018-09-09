package org.oruko.dictionary.web;

import org.oruko.dictionary.model.Definition;
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
 * The service for managing word entries
 *
 * @author Dadepo Aderemi.
 */
@Service
public class WordEntryService {

    private Integer BATCH_SIZE = 50;
    private Integer PAGE = 0;
    private Integer COUNT_SIZE = 50;

    private WordEntryRepository wordEntryRepository;
    private WordEntryFeedbackRepository wordEntryFeedbackRepository;

    /**
     * Public constructor for {@link WordEntryService} depends on instances of
     *
     * @param wordEntryRepository         Repository responsible for persisting {@link WordEntry}
     * @param wordEntryFeedbackRepository Repository responsible for persisting {@link WordEntryFeedback}
     */
    @Autowired
    public WordEntryService(WordEntryRepository wordEntryRepository,
                            WordEntryFeedbackRepository wordEntryFeedbackRepository) {
        this.wordEntryRepository = wordEntryRepository;
        this.wordEntryFeedbackRepository = wordEntryFeedbackRepository;
    }

    /**
     * Adds a new word if not present. If already present, adds the word to the
     * duplicate table.
     *
     * @param entry
     */
    public void insertTakingCareOfDuplicates(WordEntry entry) {
        String word = entry.getWord();

        if (wordPresentAsVariant(word)) {
            throw new RepositoryAccessError("Given word already exists as a variant entry");
        }

        if (alreadyExists(word)) {
            throw new RepositoryAccessError("Given word already exists in the index");
        }

        wordEntryRepository.save(entry);
    }


    /**
     * Adds a list of words in bulk if not present. If any of the word is already present, adds the word to the
     * duplicate table.
     *
     * @param entries the list of words
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
     * Returns all the feedback for a word, sorted by time submitted
     *
     * @return the feedback as a list of {@link WordEntryFeedback}
     */
    public List<WordEntryFeedback> getFeedback(WordEntry entry) {
        final Sort sort = new Sort(Sort.Direction.DESC, "submittedAt");
        return wordEntryFeedbackRepository.findByWord(entry.getWord(), sort);
    }

    /**
     * Saves {@link WordEntry}
     *
     * @param entry the entry to be saved
     */
    public WordEntry saveWord(WordEntry entry) {
        return wordEntryRepository.save(entry);
    }

    /**
     * Saves a list {@link WordEntry}
     *
     * @param entries the list of word entries to be saved
     */
    public List<WordEntry> saveWords(List<WordEntry> entries) {
        int i = 0;
        List<WordEntry> savedWords = new ArrayList<>();
        for (WordEntry entry : entries) {
            savedWords.add(this.saveWord(entry));
            i++;
            if (i == BATCH_SIZE) {
                wordEntryRepository.flush();
                i = 0;
            }
        }
        return savedWords;
    }


    /**
     * /**
     * Updates the properties with values from another {@link WordEntry}
     *
     * @param oldEntry the entry to be updated
     * @param newEntry the entry with the new value
     * @return the updated entry
     */
    public WordEntry updateWord(WordEntry oldEntry, WordEntry newEntry) {
        String oldEntryName = oldEntry.getWord();
        // update main entry
        oldEntry.update(newEntry);
        oldEntry.getDefinitions().clear();
        for (Definition definition : newEntry.getDefinitions()) {
            definition.setId(0);
            oldEntry.getDefinitions().add(definition);
        }
        return wordEntryRepository.save(oldEntry);
    }


    /**
     * Updates the properties of a list of words with values from another list of word entries
     *
     * @param wordEntries the new entries
     * @return the updated entries
     */
    public List<WordEntry> bulkUpdateWords(List<WordEntry> wordEntries) {
        List<WordEntry> updated = new ArrayList<>();

        int i = 0;
        for (WordEntry wordEntry : wordEntries) {
            WordEntry oldEntry = this.loadWord(wordEntry.getWord());
            updated.add(this.updateWord(oldEntry, wordEntry));
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
    public List<WordEntry> loadAllWords(Optional<Integer> pageNumberParam, Optional<Integer> countParam) {

        List<WordEntry> wordEntries = new ArrayList<>();
        Integer pageNumber = pageNumberParam.orElse(PAGE);
        Integer count = countParam.orElse(COUNT_SIZE);

        PageRequest request =
                new PageRequest(pageNumber == 0 ? 0 : pageNumber - 1, count, Sort.Direction.ASC, "id");

        Page<WordEntry> pages = wordEntryRepository.findAll(request);
        pages.forEach(wordEntries::add);

        return wordEntries;
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
            return this.loadAllWords(pageParam, countParam);
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
    public List<WordEntry> loadAllWords() {
        return wordEntryRepository.findAll();
    }

    /**
     * Returns the number of words in the database
     *
     * @return number of words
     */
    public Long getWordCount() {
        return wordEntryRepository.count();
    }

    /**
     * Used to retrieve a {@link WordEntry} from the repository using its known word
     *
     * @param word the word
     * @return the WordEntry
     */
    public WordEntry loadWord(String word) {
        return wordEntryRepository.findByWord(word);
    }

    /**
     * Duplicates all the word entry plus the duplicates
     */
    public void deleteAllAndDuplicates() {
        wordEntryRepository.deleteAll();
        //TODO introduce an event that all words have been deleted
    }


    /**
     * Duplicates a word entry plus its duplicates
     *
     * @param word the word to delete
     */
    public void deleteWordEntryAndDuplicates(String word) {
        WordEntry wordEntry = wordEntryRepository.findByWord(word);
        wordEntryRepository.delete(wordEntry);
    }

    /**
     * Deletes multiple word entries and their duplicates
     *
     * @param words a list of words to delete their entries and their duplicates
     */
    public void batchDeleteWordEntryAndDuplicates(List<String> words) {
        int i = 0;
        for (String word : words) {
            this.deleteWordEntryAndDuplicates(word);
            i++;

            if (i == BATCH_SIZE) {
                wordEntryRepository.flush();
                i = 0;
            }
        }
    }

    // ==================================================== Helpers ====================================================
    private boolean alreadyExists(String word) {
        WordEntry entry = wordEntryRepository.findByWord(word);
        return entry != null;
    }

    private boolean wordPresentAsVariant(String word) {
        // TODO revisit. Might end up being impacting performance
        List<WordEntry> allWords = wordEntryRepository.findAll();
        return allWords.stream().anyMatch((wordEntry) -> {
            if (wordEntry.getVariants() != null) {
                return wordEntry.getVariants().contains(word);
            }
            return false;
        });
    }
}
