package org.oruko.dictionary.website;

import org.apache.commons.lang3.StringUtils;
import org.oruko.dictionary.model.GeoLocation;
import org.oruko.dictionary.model.WordEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for the search result pages
 * Created by Dadepo Aderemi.
 */
//TODO change 'name' to 'names' in model attribute
@Controller
public class SearchResultController {

    private ApiService apiService;

    @Autowired
    public SearchResultController(ApiService apiService) {
        this.apiService = apiService;
    }

    @ModelAttribute("alphabets")
    public List<String> addAlphabetsToModel(Model map) {
        return ControllerUtil.getYorubaAlphabets();
    }

    @Value("${app.host}")
    private String host;

    /**
     * Displays the result for a single entry
     *
     * @param wordEntry the {@link WordEntry}
     * @param map       the map
     * @return the view name
     */
    @RequestMapping("/entries/{wordEntry}")
    public String showEntry(@PathVariable String wordEntry, Model map) {
        map.addAttribute("title", "Word Entry");
        map.addAttribute("host", host);

        final WordEntry word = apiService.getWord(wordEntry);
        if (word == null) {
            // no single entry found for query, return to view where search result can be displayed
            return "redirect:/entries?q=" + wordEntry;
        }

        LocalDateTime updatedAt = word.getUpdatedAt();
        LocalDateTime createdAt = word.getCreatedAt();
        if (updatedAt != null && createdAt.isEqual(updatedAt))
            word.setUpdatedAt(null);

        List<GeoLocation> location = word.getGeoLocation();
        if (location.size() > 0 && location.get(0).getRegion().equalsIgnoreCase("undefined"))
            word.getGeoLocation().clear();

        map.addAttribute("word", word);

        return "singleresult";
    }

    /**
     * Controller for page that displays multiple result for a search. i.e. ambiguous page
     *
     * @param map model the model
     * @return returns the view name
     */
    @RequestMapping("/entries")
    public String searchNameQuery(@RequestParam(value = "q", required = false) String nameQuery,
                                  Model map,
                                  RedirectAttributes redirectAttributes)
            throws UnsupportedEncodingException {
        if (nameQuery == null || nameQuery.isEmpty()) {
            return "redirect:/entries/all";
        }

        map.addAttribute("title", "Search results for query");
        List<Map<String, Object>> words = apiService.searchName(nameQuery);

        // Redirect to single-result page if there is only one search result
        if (words.size() == 1 && isEqualWithoutAccent((String) words.get(0).get("word"), nameQuery)) {
            Map<String, Object> matchWord = words.get(0);
            nameQuery = URLEncoder.encode(nameQuery, "UTF-8");
            return "redirect:/entries/" + nameQuery;
        }

        map.addAttribute("query", nameQuery);
        map.addAttribute("words", words);

        return "searchresults";
    }


    @RequestMapping("/alphabets/{alphabet}")
    public String alphabeticListing(@PathVariable String alphabet, Model map) {
        if (alphabet.length() > 2) { //gb
            //TODO ideally you should only list names by an alphabet
        }

        map.addAttribute("title", "Names listed alphabetically");
        final ArrayList<Map<String, Object>> allWordsByAlphabet = new ArrayList<>(apiService.getAllWordsByAlphabet(alphabet));

        // Remove words starting with 'gb' when the search letter is 'g'
        if ("g".equals(alphabet)) {
            allWordsByAlphabet.removeIf(name -> ((String) name.get("name")).toLowerCase().startsWith("gb"));
        }

        // TODO cant believe I can't do this from within handlebars. Revisit!
        map.addAttribute("count", allWordsByAlphabet.size());
        map.addAttribute("words", allWordsByAlphabet);
        map.addAttribute("showAlphabet", true);
        map.addAttribute("letter", alphabet);
        return "namesbyalphabet";
    }

    /**
     * Displays all the names in the dictionary. Supports pagination
     *
     * @param map model the model
     * @return returns the view name
     */
    @RequestMapping("/entries/all")
    public String showAll(Model map) {
        map.addAttribute("title", "All name entries");
        map.addAttribute("names", "Shows all entries. Supports pagination");
        return "searchresults";
    }

    private Boolean isEqualWithoutAccent(String firstName, String secondName) {
        return StringUtils.stripAccents(firstName).equalsIgnoreCase(StringUtils.stripAccents(secondName));
    }
}
