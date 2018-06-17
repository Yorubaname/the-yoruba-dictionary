package org.oruko.dictionary.search.jpa;

import org.oruko.dictionary.model.WordEntry;
import org.oruko.dictionary.model.State;
import org.oruko.dictionary.model.repository.WordEntryRepository;
import org.oruko.dictionary.search.api.IndexOperationStatus;
import org.oruko.dictionary.search.api.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JpaSearchService implements SearchService {
    private WordEntryRepository wordEntryRepository;

    @Autowired
    public JpaSearchService(WordEntryRepository wordEntryRepository) {
        this.wordEntryRepository = wordEntryRepository;
    }

    @Override
    public WordEntry getByName(String nameQuery) {
        return wordEntryRepository.findByWordAndState(nameQuery, State.PUBLISHED);
    }

    @Override
    public Set<WordEntry> search(String searchTerm) {
        Set<WordEntry> possibleFound = new LinkedHashSet<>();
        /**
         * The following approach should be taken:
         *
         * 1. First do a exact search. If found return result. If not go to 2.
         * 2. Do a search with ascii-folding. If found return result, if not go to 3
         * 3. Do a prefix search. If found, return result, if not go to 4
         * 4. Do a search based on partial match. Irrespective of outcome, proceed to 4
         *    4a - Using nGram?
         *    4b - ?
         * 5. Do a full text search against other variants. Irrespective of outcome, proceed to 6
         * 6. Do a full text search against meaning. Irrespective of outcome, proceed to 7
         * 7. Do a full text search against extendedMeaning
         */
        WordEntry exactFound = wordEntryRepository.findByWordAndState(searchTerm, State.PUBLISHED);
        if (exactFound != null) {
            return Collections.singleton(exactFound);
        }
        Set<WordEntry> startingWithSearchTerm = wordEntryRepository.findByWordStartingWithAndState(searchTerm, State.PUBLISHED);
        if (startingWithSearchTerm != null && startingWithSearchTerm.size() > 0) {
            return startingWithSearchTerm;
        }

        possibleFound.addAll(wordEntryRepository.findWordEntryByWordContainingAndState(searchTerm, State.PUBLISHED));
        possibleFound.addAll(wordEntryRepository.findWordEntryByVariantsContainingAndState(searchTerm, State.PUBLISHED));
        possibleFound.addAll(wordEntryRepository.findWordEntryByMeaningContainingAndState(searchTerm, State.PUBLISHED));

        return possibleFound;
    }

    @Override
    public Set<WordEntry> listByAlphabet(String alphabetQuery) {
        return wordEntryRepository.findByWordStartingWithAndState(alphabetQuery, State.PUBLISHED);
    }

    @Override
    public Set<String> autocomplete(String query) {
        Set<WordEntry> names = new LinkedHashSet<>();
        Set<String> nameToReturn = new LinkedHashSet<>();
        // TODO calling the db in a for loop might not be a terribly good idea. Revist
        for (int i=2; i<query.length() + 1; i++) {
            String searchTerm = query.substring(0, i);
            names.addAll(wordEntryRepository.findByWordStartingWithAndState(searchTerm, State.PUBLISHED));
        }
        Set<WordEntry> otherParts = wordEntryRepository.findWordEntryByWordContainingAndState(query, State.PUBLISHED);
        names.addAll(otherParts);
        names.forEach(name -> {
            nameToReturn.add(name.getWord());
        });
        return nameToReturn;
    }

    @Override
    public Integer getSearchableNames() {
        return wordEntryRepository.countByState(State.PUBLISHED);
    }

    @Override
    public IndexOperationStatus bulkIndexName(List<WordEntry> entries) {
        if (entries.size() == 0) {
            return new IndexOperationStatus(false, "Cannot index an empty list");
        }
        wordEntryRepository.save(entries);
        return new IndexOperationStatus(true, "Bulk indexing successfully. Indexed the following names "
                + String.join(",", entries.stream().map(WordEntry::getWord).collect(Collectors.toList())));
    }

    public IndexOperationStatus removeFromIndex(String name) {
        WordEntry foundName = wordEntryRepository.findByWordAndState(name, State.PUBLISHED);
        if (foundName == null) {
            return new IndexOperationStatus(false, "Published Name not found");
        }
        foundName.setState(State.UNPUBLISHED);
        wordEntryRepository.save(foundName);
        return new IndexOperationStatus(true, name + " removed from index");
    }

    @Override
    public IndexOperationStatus bulkRemoveByNameFromIndex(List<String> names) {
        if (names.size() == 0) {
            return new IndexOperationStatus(false, "Cannot index an empty list");
        }
        List<WordEntry> nameEntries = names.stream().map(name -> wordEntryRepository.findByWordAndState(name, State.PUBLISHED))
                .collect(Collectors.toList());


        List<WordEntry> namesUnpublished = nameEntries.stream().map(name -> {
            name.setState(State.UNPUBLISHED);
            return name;
        }).collect(Collectors.toList());

        wordEntryRepository.save(namesUnpublished);
        return new IndexOperationStatus(true, "Successfully. "
                + "Removed the following names from search index "
                + String.join(",", names));
    }

    @Override
    public IndexOperationStatus bulkRemoveFromIndex(List<WordEntry> nameEntries) {
        List<WordEntry> namesUnpublished = nameEntries.stream()
                .peek(name -> name.setState(State.UNPUBLISHED))
                .collect(Collectors.toList());

        wordEntryRepository.save(namesUnpublished);
        return new IndexOperationStatus(true, "Successfully. "
                + "Removed the following names from search index "
                + String.join(",", nameEntries.stream().map(WordEntry::getWord).collect(Collectors.toList())));
    }
}
