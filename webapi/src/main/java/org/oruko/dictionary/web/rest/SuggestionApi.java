package org.oruko.dictionary.web.rest;

import org.oruko.dictionary.model.WordEntry;
import org.oruko.dictionary.web.SuggestedNameService;
import org.oruko.dictionary.web.exception.GenericApiCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;

/**
 * Endpoint for interaction with suggested names
 *
 * @author Dadepo Aderemi.
 */
@RestController
@RequestMapping("/v1/suggestions")
public class SuggestionApi {
    SuggestedNameService suggestedNameService;

    /**
     * Constructor for {@link SuggestionApi}
     * @param suggestedNameService the {@link SuggestedNameService}
     */
    @Autowired
    public SuggestionApi(SuggestedNameService suggestedNameService) {
        this.suggestedNameService = suggestedNameService;
    }

    /**
     * Endpoint for retrieving metadata information for suggested names
     *
     * @return a {@link ResponseEntity} with the response message
     */
    @RequestMapping(value = "/meta", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getSuggestedMetaData() {
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("totalSuggestedNames", suggestedNameService.countAll());
        return new ResponseEntity<>(metaData, HttpStatus.OK);
    }


    /**
     * End point for receiving suggested names into the database. The names
     * suggested won't be added to the main database or search index until
     * approved by admin of the system.
     * @param suggestedName the name suggested
     * @param bindingResult the {@link BindingResult}
     * @return {@link org.springframework.http.ResponseEntity} with message if successful or not
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> suggestName(@Valid @RequestBody WordEntry suggestedName,
                                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new GenericApiCallException(formatErrorMessage(bindingResult), HttpStatus.BAD_REQUEST);
        }
        suggestedNameService.saveName(suggestedName);
        return new ResponseEntity<>(response("Suggested Name successfully added"), HttpStatus.CREATED);
    }

    /**
     * Returns all the suggested names
     * @return a {@link ResponseEntity} with all suggested names
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<WordEntry> getAllSuggestedNames() {
        return suggestedNameService.findAll();
    }

    /**
     * End point for deleting suggested name
     * @param id id of the suggested name to delete
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, String>> deleteSuggestedName(@PathVariable Long id) {
        boolean deleted = suggestedNameService.delete(id);
        return deleted ? new ResponseEntity<>(response("Suggested name with " + id + " successfully deleted"),
                HttpStatus.NO_CONTENT) : new ResponseEntity<>(response("Suggested name with " + id + " was not found"),
                                    HttpStatus.BAD_REQUEST);
    }

    /**
     * End point for deleting all suggested name
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, String>> deleteAllSuggestions() {
        suggestedNameService.deleteAll();
        return new ResponseEntity<>(response("All suggested names have been deleted"), HttpStatus.OK);
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
