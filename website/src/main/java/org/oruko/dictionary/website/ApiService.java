package org.oruko.dictionary.website;

import org.oruko.dictionary.model.WordEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with the NameAPI for the website
 * <p>
 * Created by Dadepo Aderemi.
 */
@Service
public class ApiService {

    private static RestTemplate restTemplate = new RestTemplate();

    public static String APIPATH;

    @Autowired
    public void setAPIPATH(@Value("${app.host}") String hostName,
                           @Value("${server.port}") String port, @Value("${server.ssl.enabled:#{false}}") boolean sslEnabled) {
        String apiPath = (sslEnabled ? "https" : "http") + "://" + hostName + ":" + port + "/v1";
        ApiService.APIPATH = apiPath;
    }


    public List<Map<String, Object>> getAllNames() {
        return Arrays.asList(restTemplate.getForObject(APIPATH + "/names", Map[].class));
    }

    @Cacheable("querySearchResult")
    public WordEntry getWord(String nameQuery) {
        return restTemplate.getForObject(APIPATH + "/search/" + nameQuery, WordEntry.class);
    }

    @Cacheable("names")
    public List<Map<String, Object>> searchName(String nameQuery) {
        return Arrays.asList(restTemplate.getForObject(APIPATH + "/search/?q=" + nameQuery, Map[].class));
    }

    public List<Map<String, Object>> getAllWordsByAlphabet(String alphabet) {
        return Arrays.asList(restTemplate.getForObject(APIPATH + "/search/alphabet/" + alphabet, Map[].class));
    }

    public Integer getIndexedNameCount() {
        final Map<String, Integer> countMap = restTemplate.getForObject(APIPATH + "/search/meta", Map.class);
        return countMap.get("totalPublishedNames");
    }

    public Map<String, String[]> getSearchActivity() {
        return restTemplate.getForObject(APIPATH + "/search/activity/all", Map.class);
    }

    public List<Map<String, Object>> getGeoLocations() {
        return Arrays.asList(restTemplate.getForObject(APIPATH + "/admin/geolocations", Map[].class));
    }
}
