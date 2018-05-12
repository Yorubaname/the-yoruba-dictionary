package org.oruko.dictionary.util;

import org.oruko.dictionary.model.GeoLocation;
import org.oruko.dictionary.model.WordEntry;
import org.oruko.dictionary.model.State;
import org.oruko.dictionary.model.Etymology;
import org.oruko.dictionary.model.repository.GeoLocationRepository;
import org.oruko.dictionary.model.repository.WordEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Initialize the database and ElasticSearch with some dummy data
 *
 * Created by Dadepo Aderemi.
 */
@Component
public class DataImporter {

    @Value("${app.host}")
    private String host;

    @Autowired
    private GeoLocationRepository geoLocationRepository;

    @Autowired
    private WordEntryRepository wordEntryRepository;


    @PostConstruct
    public void initializeData() {

        if (geoLocationRepository.findAll().size() == 0) {
            initGeoLocation();
        }
        /**
         * Only initialize the database only when in dev
         * //TODO move this to profiles
         */
        if (host.equalsIgnoreCase("localhost") && wordEntryRepository.count() == 0) {
            List<WordEntry> nameEntries = initializeDb();
        }
    }

    private List<WordEntry> initializeDb() {
        Etymology dummyEtymology1 = new Etymology();
        dummyEtymology1.setPart("first section");
        dummyEtymology1.setMeaning("first section meaning");

        Etymology dummyEtymology2 = new Etymology();
        dummyEtymology2.setPart("second section");
        dummyEtymology2.setMeaning("second section meaning");

        ArrayList<Etymology> etymology = new ArrayList<>();
        etymology.add(dummyEtymology1);
        etymology.add(dummyEtymology2);

        // sample name entries
        WordEntry lagbaja = new WordEntry("lagbaja");
        lagbaja.setMeaning("This is dummy meaning for Lagbaja");
        lagbaja.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY"),
                                             new GeoLocation("I DO NOT KNOW", "UNDEFINED")));
        lagbaja.setEtymology(etymology);
        lagbaja.setState(State.NEW);


        WordEntry tamedo = new WordEntry("tamedo");
        tamedo.setMeaning("This is dummy meaning for tamedo");
        tamedo.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        tamedo.setEtymology(etymology);
        tamedo.setState(State.NEW);

        WordEntry koko = new WordEntry("koko");
        koko.setMeaning("This is dummy meaning for koko");
        koko.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        koko.setEtymology(etymology);
        koko.setState(State.MODIFIED);


        WordEntry tola = new WordEntry("tola");
        tola.setMeaning("This is dummy meaning for tola");
        tola.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        tola.setEtymology(etymology);
        tola.setState(State.MODIFIED);


        WordEntry dadepo = new WordEntry("dadepo");
        dadepo.setMeaning("This is dummy meaning for dadepo");
        dadepo.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        dadepo.setEtymology(etymology);
        dadepo.setState(State.MODIFIED);

        WordEntry bolanle = new WordEntry("Bọ́lánlé");
        bolanle.setMeaning("This is dummy meaning for Bọ́lánlé");
        bolanle.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        bolanle.setEtymology(etymology);
        bolanle.setState(State.PUBLISHED);


        WordEntry bimpe = new WordEntry("Bimpe");
        bimpe.setMeaning("This is dummy meaning for Bimpe");
        bimpe.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        bimpe.setEtymology(etymology);
        bimpe.setState(State.PUBLISHED);

        WordEntry ade0 = new WordEntry("Ade");
        ade0.setMeaning("This is dummy meaning for ade");
        ade0.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        ade0.setEtymology(etymology);
        ade0.setState(State.NEW);

        WordEntry ade1 = new WordEntry("Adewale");
        ade1.setMeaning("This is dummy meaning for adewale");
        ade1.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        ade1.setEtymology(etymology);
        ade1.setState(State.NEW);

        WordEntry ade2 = new WordEntry("Adekunle");
        ade2.setMeaning("This is dummy meaning for Adekunle");
        ade2.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        ade2.setEtymology(etymology);
        ade2.setState(State.NEW);

        WordEntry ade3 = new WordEntry("Adetunji");
        ade3.setMeaning("This is dummy meaning for Adetunji");
        ade3.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        ade3.setEtymology(etymology);
        ade3.setState(State.NEW);

        WordEntry ade4 = new WordEntry("Adedotun");
        ade4.setMeaning("This is dummy meaning for Adedotun");
        ade4.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        ade4.setEtymology(etymology);
        ade4.setState(State.NEW);

        /**
         * Sample for search beahviour with variants and otherlanguages
         */
        WordEntry omowumi = new WordEntry("Omowumi");
        omowumi.setInOtherLanguages("omewami");
        omowumi.setMeaning("This is dummy meaning for Omowumi");
        omowumi.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        omowumi.setEtymology(etymology);
        omowumi.setState(State.NEW);


        WordEntry omolabi = new WordEntry("Omolabi");
        omolabi.setMeaning("This is dummy meaning for omolabi");
        omolabi.setGeoLocation(Arrays.asList(new GeoLocation("IBADAN", "NWY")));
        omolabi.setEtymology(etymology);
        omolabi.setState(State.NEW);

        wordEntryRepository.save(lagbaja);
        wordEntryRepository.save(tamedo);
        wordEntryRepository.save(koko);
        wordEntryRepository.save(tola);
        wordEntryRepository.save(dadepo);
        wordEntryRepository.save(bolanle);
        wordEntryRepository.save(bimpe);
        wordEntryRepository.save(ade0);
        wordEntryRepository.save(ade1);
        wordEntryRepository.save(ade2);
        wordEntryRepository.save(ade3);
        wordEntryRepository.save(ade4);
        wordEntryRepository.save(omowumi);
        wordEntryRepository.save(omolabi);

        return Arrays.asList(lagbaja, tamedo, koko, tola, dadepo, bolanle,
                ade0, ade1, ade2, ade3, ade4, omowumi, omolabi);
    }

    private void initGeoLocation() {
        // North-West Yoruba (NWY): Abẹokuta, Ibadan, Ọyọ, Ogun and Lagos (Eko) areas
        // Central Yoruba (CY): Igbomina, Yagba, Ilésà, Ifẹ, Ekiti, Akurẹ, Ẹfọn, and Ijẹbu areas.
        // South-East Yoruba (SEY): Okitipupa, Ilaje, Ondo, Ọwọ, Ikarẹ, Ṣagamu, and parts of Ijẹbu.

        geoLocationRepository.save(new GeoLocation("ABEOKUTA", "NWY"));
        geoLocationRepository.save(new GeoLocation("IBADAN", "NWY"));
        geoLocationRepository.save(new GeoLocation("OYO", "OYO"));
        geoLocationRepository.save(new GeoLocation("OGUN", "OGN"));
        geoLocationRepository.save(new GeoLocation("EKO", "EKO"));

        geoLocationRepository.save(new GeoLocation("IGBOMINA", "CY"));
        geoLocationRepository.save(new GeoLocation("YAGBA", "CY"));
        geoLocationRepository.save(new GeoLocation("ILESHA", "CY"));
        geoLocationRepository.save(new GeoLocation("IFE", "CY"));
        geoLocationRepository.save(new GeoLocation("EKITI", "CY"));
        geoLocationRepository.save(new GeoLocation("AKURE", "CY"));
        geoLocationRepository.save(new GeoLocation("EFON", "CY"));
        geoLocationRepository.save(new GeoLocation("IJEBU", "CY"));

        geoLocationRepository.save(new GeoLocation("OKITIPUPA", "SEY"));
        geoLocationRepository.save(new GeoLocation("IJALE", "SEY"));
        geoLocationRepository.save(new GeoLocation("ONDO", "SEY"));
        geoLocationRepository.save(new GeoLocation("OWO", "SEY"));
        geoLocationRepository.save(new GeoLocation("IKARE", "SEY"));
        geoLocationRepository.save(new GeoLocation("SAGAMU", "SEY"));
        geoLocationRepository.save(new GeoLocation("GENERAL/NOT LOCATION SPECIFIC", "GENERAL"));
        geoLocationRepository.save(new GeoLocation("I DO NOT KNOW", "UNDEFINED"));

        geoLocationRepository.save(new GeoLocation("FOREIGN: ARABIC", "FOREIGN_ARABIC"));
        geoLocationRepository.save(new GeoLocation("FOREIGN: GENERAL", "FOREIGN_GENERAL"));
    }
}
