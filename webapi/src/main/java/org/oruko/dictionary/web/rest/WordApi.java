package org.oruko.dictionary.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.oruko.dictionary.events.EventPubService;
import org.oruko.dictionary.events.WordDeletedEvent;
import org.oruko.dictionary.importer.ImporterInterface;
import org.oruko.dictionary.model.GeoLocation;
import org.oruko.dictionary.model.WordEntry;
import org.oruko.dictionary.model.State;
import org.oruko.dictionary.model.repository.GeoLocationRepository;
import org.oruko.dictionary.web.GeoLocationTypeConverter;
import org.oruko.dictionary.web.WordEntryService;
import org.oruko.dictionary.web.event.WordUploadStatus;
import org.oruko.dictionary.web.exception.GenericApiCallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * End point for inserting and retrieving WordDto Entries
 * This would be the end point the clients would interact with to get words in and out of the dictionary
 * Created by dadepo on 2/12/15.
 */
@RestController
public class WordApi {

    private Logger logger = LoggerFactory.getLogger(WordApi.class);

    private ImporterInterface importerInterface;
    private WordEntryService entryService;
    private GeoLocationRepository geoLocationRepository;
    private WordUploadStatus wordUploadStatus;
    private EventPubService eventPubService;


    /**
     * Public constructor for {@link WordApi}
     * @param importerInterface an implementation of {@link ImporterInterface} used for adding words in files
     * @param entryService an instance of {@link WordEntryService} representing the service layer
     * @param geoLocationRepository an instance of {@link GeoLocationRepository} for persiting {@link GeoLocation}
     */
    @Autowired
    public WordApi(ImporterInterface importerInterface, WordEntryService entryService,
                   GeoLocationRepository geoLocationRepository,
                   WordUploadStatus wordUploadStatus,
                   EventPubService eventPubService) {
        this.importerInterface = importerInterface;
        this.entryService = entryService;
        this.geoLocationRepository = geoLocationRepository;
        this.wordUploadStatus = wordUploadStatus;
        this.eventPubService = eventPubService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(GeoLocation.class, new GeoLocationTypeConverter(geoLocationRepository));
    }

    /**
     * End point that is used to add a {@link WordEntry}.
     * @param entry the {@link WordEntry}
     * @param bindingResult {@link org.springframework.validation.BindingResult} used to capture result of validation
     * @return {@link org.springframework.http.ResponseEntity} with string containing error message.
     * "success" is returned if no error
     */
    @RequestMapping(value = "/v1/words", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> addWord(@Valid @RequestBody WordEntry entry,
                                                       BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            if (entry.getState() == null) {
                entry.setState(State.NEW);
            }

            if (!State.NEW.equals(entry.getState())) {
                // You can only add a word to the system with its state NEW
                throw new GenericApiCallException("Invalid State: A new entry needs to have the NEW state");
            }
            entry.setWord(entry.getWord().trim().toLowerCase());
            entryService.insertTakingCareOfDuplicates(entry);
            return new ResponseEntity<>(response("Word successfully added"), HttpStatus.CREATED);
        }
        throw new GenericApiCallException(formatErrorMessage(bindingResult));
    }

    /**
     * Endpoint for retrieving metadata information
     *
     * @return a {@link ResponseEntity} with the response message
     */
    @RequestMapping(value = "/v1/words/meta", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getMetaData() {

        final List<WordEntry> wordEntries = entryService.loadAllWords();
        long totalWords = ((Integer) wordEntries.size()).longValue();

        final long totalModifiedWords = wordEntries.stream()
                                                   .filter(wordEntry -> State.MODIFIED.equals(wordEntry.getState()))
                                                   .count();
        final long totalNewWords = wordEntries.stream()
                                              .filter(wordEntry -> State.NEW.equals(wordEntry.getState()))
                                              .count();
        final long totalPublishedWords = wordEntries.stream()
                                                    .filter(wordEntry -> State.PUBLISHED.equals(wordEntry.getState()))
                                                    .count();
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("totalWords", totalWords);
        metaData.put("totalNewWords", totalNewWords);
        metaData.put("totalModifiedWords", totalModifiedWords);
        metaData.put("totalPublishedWords", totalPublishedWords);

        return new ResponseEntity<>(metaData, HttpStatus.OK);
    }

    /**
     * Get words that has been persisted. Supports ability to specify the count of words to return and the offset
     * @param pageParam a {@link Integer} representing the page (offset) to start the
     *                  result set from. 0 if none is given
     * @param countParam a {@link Integer} the number of words to return. 50 is none is given
     * @return the list of {@link WordEntry}
     */
    @RequestMapping(value = "/v1/words", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<WordEntry> getAllWords(@RequestParam("page") final Optional<Integer> pageParam,
                                       @RequestParam("count") final Optional<Integer> countParam,
                                       @RequestParam("all") final Optional<Boolean> all,
                                       @RequestParam("submittedBy") final Optional<String> submittedBy,
                                       @RequestParam("state") final Optional<State> state) {

        List<WordEntry> allWordEntries;

        if (all.isPresent() && all.get()) {
            if (state.isPresent()) {
                allWordEntries = entryService.loadAllByState(state);
            } else {
                allWordEntries = entryService.loadAllWords();
            }
        } else {
            allWordEntries = entryService.loadByState(state, pageParam, countParam);
        }

        List<WordEntry> words = new ArrayList<>(allWordEntries);

        // for filtering based on value of submitBy
        Predicate<WordEntry> filterBasedOnSubmitBy = (word) -> submittedBy
                .map(s -> word.getSubmittedBy().trim().equalsIgnoreCase(s.trim()))
                .orElse(true);

        return words.stream()
                    .filter(filterBasedOnSubmitBy)
                    .collect(Collectors.toCollection(ArrayList::new));

    }

    /**
     * Get the details of a word
     * @param word the word whose details needs to be retrieved
     * @return a word serialized to a jason string
     * @throws JsonProcessingException json processing exception
     */
    @RequestMapping(value = "/v1/words/{word}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getWord(@RequestParam("feedback") final Optional<Boolean> feedback,
                          @PathVariable String word) throws JsonProcessingException {
        WordEntry wordEntry = entryService.loadWord(word);
        if (wordEntry == null) {
            String errorMsg = "#WORD not found in the database".replace("#WORD", word);
            throw new GenericApiCallException(errorMsg);
        }

        HashMap<String, Object> wordEntries = new HashMap<>();
        wordEntries.put("mainEntry", wordEntry);

        if (feedback.isPresent() && (feedback.get() == true)) {
            wordEntries.put("feedback", entryService.getFeedback(wordEntry));
        }

        if (wordEntries.size() == 1 && wordEntries.get("mainEntry") != null) {
            return wordEntries.get("mainEntry");
        }

        return wordEntries;
    }


    /**
     * End point that is used to update a {@link WordEntry}.
     * @param newWordEntry the {@link WordEntry}
     * @param bindingResult {@link org.springframework.validation.BindingResult} used to capture result of validation
     * @return {@link org.springframework.http.ResponseEntity} with string containting error message.
     * "success" is returned if no error
     */
    @RequestMapping(value = "/v1/words/{word}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.PUT)
    public ResponseEntity<Map> updateWord(@PathVariable String word,
                                             @Valid @RequestBody WordEntry newWordEntry,
                                             BindingResult bindingResult) {
        //TODO tonalMark is returning null on update. Fix
        if (!bindingResult.hasErrors()) {

            WordEntry oldWordEntry = entryService.loadWord(word);

            if (oldWordEntry == null) {
                throw new GenericApiCallException(word + " not in database", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            entryService.updateWord(oldWordEntry, newWordEntry);
            return new ResponseEntity<>(response("Word successfully updated"), HttpStatus.CREATED);
        }

        throw new GenericApiCallException(formatErrorMessage(bindingResult),
                                          HttpStatus.BAD_REQUEST);
    }


    /**
     * Endpoint for uploading words via spreadsheet
     *
     * @param multipartFile the spreadsheet file
     * @return the Import status
     * @throws JsonProcessingException Json processing exception
     */
    @RequestMapping(value = "/v1/words/upload", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("wordFiles") MultipartFile multipartFile)
            throws JsonProcessingException {
        Assert.state(!multipartFile.isEmpty(), "You can't upload an empty file");

        try {
            File file = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
            multipartFile.transferTo(file);

            // perform the importation of words in a seperate thread
            // client can poll /v1/words/uploading?q=progress for upload progress
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                importerInterface.importFile(file);
                file.delete();
            });

            return new ResponseEntity<>(response("File successfully imported"), HttpStatus.ACCEPTED);
        } catch (IOException e) {
            logger.warn("Failed to import File with error {}", e.getMessage());
            throw new GenericApiCallException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint that returns if a word uploading is ongoing and if so, provides
     * the number of total words to be uploaded and the numbers already uploaded.
     * @param parameter query parameter. Supports "progress"
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/v1/words/uploading", method = RequestMethod.GET)
    public ResponseEntity<WordUploadStatus> uploadProgress(@RequestParam("q") Optional<String> parameter)
            throws JsonProcessingException {
        if (parameter.isPresent()) {
            switch (parameter.get()) {
                case "progress":
                    return new ResponseEntity<>(wordUploadStatus, HttpStatus.OK);
                default:
                    throw new GenericApiCallException("query parameter [" + parameter.get() + "] not supported",
                                                      HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        throw  new GenericApiCallException("query parameter missing", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Endpoint for batch uploading of words. Words are sent as array of json from the client
     * @param wordEntries the array of {@link WordEntry}
     * @param bindingResult {@link org.springframework.validation.BindingResult} used to capture result of validation
     * @return {@link org.springframework.http.ResponseEntity} with string containting error message.
     * "success" is returned if no error
     */
    @RequestMapping(value = "/v1/words/batch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity< Map<String, String>> addWord(@Valid @RequestBody WordEntry[] wordEntries,
                                                        BindingResult bindingResult) {
        if (!bindingResult.hasErrors() && wordEntries.length != 0) {
            entryService.bulkInsertTakingCareOfDuplicates(Arrays.asList(wordEntries));
            return new ResponseEntity<>(response("Words successfully imported"), HttpStatus.CREATED);
        }
        throw new GenericApiCallException(formatErrorMessage(bindingResult), HttpStatus.BAD_REQUEST);
    }


    /**
     * Endpoint for batch updating  of words. Words are sent as array of json from the client
     * @param wordEntries the array of {@link WordEntry}
     * @param bindingResult {@link org.springframework.validation.BindingResult} used to capture result of validation
     * @return {@link org.springframework.http.ResponseEntity} with string containing error message.
     * "success" is returned if no error
     */
    @RequestMapping(value = "/v1/words/batch", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity< Map<String, String>> updateWords(@Valid @RequestBody WordEntry[] wordEntries,
                                                         BindingResult bindingResult) {
        if (!bindingResult.hasErrors() && wordEntries.length != 0) {

            //TODO refactor into a method
            List<WordEntry> notFoundWords = Stream.of(wordEntries)
                                             .filter(entry -> entryService.loadWord(entry.getWord()) == null)
                                            .collect(Collectors.toList());

            List<WordEntry> foundWords = new ArrayList<>(Arrays.asList(wordEntries));
            foundWords.removeAll(notFoundWords);

            if (foundWords.size() == 0) {
                return new ResponseEntity<>(response("none of the words was found in the repository so not indexed"),
                                            HttpStatus.BAD_REQUEST);
            }
            entryService.bulkUpdateWords(foundWords);

            List<String> notFound = notFoundWords.stream()
                                                 .map(WordEntry::getWord)
                                                 .collect(Collectors.toList());

            List<String> found = foundWords.stream()
                                             .map(WordEntry::getWord)
                                             .collect(Collectors.toList());

            String responseMessage = String.join(",", found) + " updated. ";

            if (notFound.size() > 0) {
                responseMessage += String.join(",",notFound) + " not updated as they were not found in the database";
            }

            return new ResponseEntity<>(response(responseMessage), HttpStatus.CREATED);
        }

        throw new GenericApiCallException(formatErrorMessage(bindingResult), HttpStatus.BAD_REQUEST);
    }

    /**
     * End points for deleting ALL words (and their duplicates) from the database
     * @return {@link org.springframework.http.ResponseEntity} with string containing error message.
     * "success" is returned if no error
     */
    @RequestMapping(value = "/v1/words",
            method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> deleteAllWords() {
        entryService.deleteAllAndDuplicates();
        return new ResponseEntity<>(response("Words deleted"), HttpStatus.OK);
    }

    /**
     * End point for deleting a word (and its duplicates) from the database.
     * @param word the word to delete
     * @return {@link org.springframework.http.ResponseEntity} with string containing status message
     */
    @RequestMapping(value = "/v1/words/{word}",
            method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity< Map<String, String>> deleteWord(@PathVariable String word) {
        if (entryService.loadWord(word) == null) {
            throw new GenericApiCallException(word + " not found in the system so cannot be deleted");
        }
        entryService.deleteWordEntryAndDuplicates(word);
        publishWordsDeletedEvent(Collections.singletonList(word));
        return new ResponseEntity<>(response(word + " Deleted"), HttpStatus.OK);
    }

    /**
     * Endpoint for deleting a list of words
     *
     * @param words the list of words to delete
     * @return {@link org.springframework.http.ResponseEntity} with string containing status message
     */
    @RequestMapping(value = "/v1/words/batch",
            method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity< Map<String, String>> batchDeleteWord(@RequestBody String[] words) {

        List<String> notFoundWords = Stream.of(words)
                                     .filter(entry -> entryService.loadWord(entry) == null)
                                     .collect(Collectors.toCollection(ArrayList::new));

        List<String> foundWords = new ArrayList<>(Arrays.asList(words));
        foundWords.removeAll(notFoundWords);

        if (foundWords.size() == 0) {
            return new ResponseEntity<>(response("No deletion as none of the words were found in the database."),
                                        HttpStatus.BAD_REQUEST);
        }

        entryService.batchDeleteWordEntryAndDuplicates(foundWords);
        publishWordsDeletedEvent(foundWords);

        String responseMessage = String.join(",",foundWords) + " deleted. ";
        if (notFoundWords.size() > 0) {
            responseMessage += String.join(",",notFoundWords) + " not deleted as they were not found in the database";
        }
        return new ResponseEntity<>(response(responseMessage), HttpStatus.OK);
    }

    private void publishWordsDeletedEvent(List<String> foundWords) {
        for (String word: foundWords) {
            eventPubService.publish(new WordDeletedEvent(word));
        }
    }

    //=====================================Helpers=========================================================//

    private String formatErrorMessage(BindingResult bindingResult) {
        StringBuilder builder = new StringBuilder();
        for (FieldError error : bindingResult.getFieldErrors()) {
            builder.append(error.getField()).append(" ").append(error.getDefaultMessage()).append(" ");
        }
        return builder.toString();
    }


    private HashMap<String, String> response(String value) {
        HashMap<String, String> response = new HashMap<>();
        response.put("message", value);
        return response;
    }
}
