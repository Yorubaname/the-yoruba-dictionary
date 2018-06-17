package org.oruko.dictionary.importer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.oruko.dictionary.events.EventPubService;
import org.oruko.dictionary.events.NameUploadedEvent;
import org.oruko.dictionary.model.GeoLocation;
import org.oruko.dictionary.model.WordEntry;
import org.oruko.dictionary.model.repository.GeoLocationRepository;
import org.oruko.dictionary.model.repository.WordEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Importer for importing names from an excel sheet into
 * {@link WordEntry}
 *
 * @author Dadepo Aderemi.
 */
@Component
public class ExcelImporter implements ImporterInterface {

    private Logger logger = LoggerFactory.getLogger(ExcelImporter.class);

    private WordEntryRepository wordEntryRepository;
    private GeoLocationRepository geoLocationRepository;
    private ImporterValidator validator;
    private EventPubService eventPubService;
    private ColumnOrder columnOrder;


    @Autowired
    public void setEventPubService(EventPubService eventPubService) {
        this.eventPubService = eventPubService;
    }

    @Autowired
    public void setWordEntryRepository(WordEntryRepository wordEntryRepository) {
        this.wordEntryRepository = wordEntryRepository;
    }

    @Autowired
    public void setGeoLocationRepository(
            GeoLocationRepository geoLocationRepository) {
        this.geoLocationRepository = geoLocationRepository;
    }

    @Autowired
    public void setValidator(ImporterValidator validator) {
        this.validator = validator;
    }

    @Autowired
    public void setColumnOrder(ColumnOrder columnOrder) {
        this.columnOrder = columnOrder;
    }
    

    @Override
    public ImportStatus importFile(File fileSource) {
        ImportStatus status = new ImportStatus();
        NameUploadedEvent nameUploadedEvent = new NameUploadedEvent();

        XSSFSheet sheet;
        try {
            sheet = getSheet(fileSource, 0);
        } catch (IOException e) {
            logger.error("Failed to import file {} with error {}", fileSource.getAbsoluteFile(), e.getMessage());
            status.setErrorMessages(e.getMessage());
            return status;
        } catch (InvalidFormatException e) {
            logger.error("Failed to import file {} with error {}", fileSource.getAbsoluteFile(), e.getMessage());
            status.setErrorMessages(e.getMessage());
            return status;
        }

        int totalNumberOfNames = sheet.getPhysicalNumberOfRows() - 1; // removes the header row
        nameUploadedEvent.setTotalNumberOfNames(totalNumberOfNames);
        nameUploadedEvent.isUploading(true);

        if (validator.isColumnNameInOrder(sheet)) {
            Iterator<Row> rowIterator = sheet.rowIterator();

            while (rowIterator.hasNext()) {
                boolean fieldIsEmpty = true;
                String name = "";
                String pronunciation = "";
                String ipaNotation = "";
                String variant = "";
                String syllable = "";
                String meaning = "";
                String extendedMeaning = "";
                String morphology = "";
                String etymology = "";
                String geoLocation = "";
                String media = "";

                Row row = rowIterator.next();
                if (row.getRowNum() == 0) {
                    continue;
                }

                WordEntry wordEntry = new WordEntry();

                Cell nameCell = row.getCell(columnOrder.getColumnOrder().inverse().get("name"));
                if (nameCell != null) {
                    name = nameCell.toString();
                    if (!name.isEmpty()) {
                        fieldIsEmpty = false;
                        wordEntry.setWord(name.trim());
                    } else {
                        // if name is empty then the row is nullified, so skip
                        continue;
                    }

                }
                Cell pronunciationCell = row.getCell(columnOrder.getColumnOrder().inverse().get("pronunciation"));
                if (pronunciationCell != null) {
                    pronunciation = pronunciationCell.toString();
                    if (!pronunciation.isEmpty()) {
                        fieldIsEmpty = false;
                        wordEntry.setPronunciation(pronunciation.trim());
                    }
                }

                Cell ipaCell = row.getCell(columnOrder.getColumnOrder().inverse().get("ipa_notation"));
                if (ipaCell != null) {
                    ipaNotation = ipaCell.toString();
                    if (!ipaNotation.isEmpty()) {
                        fieldIsEmpty = false;
                        wordEntry.setIpaNotation(ipaNotation.trim());
                    }
                }

                Cell syllableCell = row.getCell(columnOrder.getColumnOrder().inverse().get("syllable"));
                if (syllableCell != null) {
                    syllable = syllableCell.toString();
                    if (!syllable.isEmpty()) {
                        fieldIsEmpty = false;
                        wordEntry.setSyllables(syllable.trim());
                    }
                }

                Cell meaningCell = row.getCell(columnOrder.getColumnOrder().inverse().get("meaning"));
                if (meaningCell != null) {
                    meaning = meaningCell.toString();
                    if (!meaning.isEmpty()) {
                        fieldIsEmpty = false;
                        wordEntry.setMeaning(meaning.trim());
                    }

                }

                Cell morphologyCell = row.getCell(columnOrder.getColumnOrder().inverse().get("morphology"));
                if (morphologyCell != null) {
                    morphology = morphologyCell.toString();
                    if (!morphology.isEmpty()) {
                        fieldIsEmpty = false;
                        wordEntry.setMorphology(morphology.trim());
                    }
                }

                Cell etymologyCell = row.getCell(columnOrder.getColumnOrder().inverse().get("etymology"));
                if (etymologyCell != null) {
                    etymology = etymologyCell.toString();
                    if (!etymology.isEmpty()) {
                        // TODO define format for etymology in spreadsheet
                        fieldIsEmpty = false;
                        wordEntry.setEtymology(null);
                    }
                }

                Cell geoLocationCell = row.getCell(columnOrder.getColumnOrder().inverse().get("geo_location"));
                if (geoLocationCell != null) {
                    geoLocation = geoLocationCell.toString();
                    if (!geoLocation.isEmpty()) {
                        fieldIsEmpty = false;
                        wordEntry.setGeoLocation(getGeoLocation(geoLocation));
                    }
                }


                Cell mediaCell = row.getCell(columnOrder.getColumnOrder().inverse().get("media"));
                if (mediaCell != null) {
                    media = mediaCell.toString();
                    if (!media.isEmpty()) {
                        fieldIsEmpty = false;
                        wordEntry.setMedia(media.trim());
                    }
                }

                if (fieldIsEmpty) {
                    continue;
                }

                try {
                    if (alreadyExists(name)) {
                        logger.info("Name {} already exists in the index. Skipping...", name);
                    } else {
                        wordEntryRepository.save(wordEntry);
                        status.incrementNumberOfNames();
                    }
                } catch (Exception e) {
                    logger.debug("Exception while uploading name entry with name {}", name, e);
                }

                nameUploadedEvent.setTotalUploaded(status.getNumberOfNamesUpload());
                eventPubService.publish(nameUploadedEvent);
            }
        } else {
            status.setErrorMessages("Columns not in order. Should be in the following order {ORDER}"
                                            .replace("{ORDER}", columnOrder.getColumnOrderAsString()));
        }

        // publishes event that signifies end of uploading
        nameUploadedEvent.isUploading(false);
        eventPubService.publish(nameUploadedEvent);
        return status;
    }



    // ==================================================== Helpers ====================================================

    private ArrayList<GeoLocation> getGeoLocation(String locations) {
        final String[] locationArrays = locations.split(",");
        final ArrayList<GeoLocation> locationList = new ArrayList<>();
        for (String aLocation:locationArrays) {
            locationList.add(geoLocationRepository.findByPlace(aLocation));
        }
        return locationList;
    }

    private XSSFSheet getSheet(File file, int sheetIndex) throws IOException, InvalidFormatException {
        XSSFWorkbook wb = new XSSFWorkbook(file);
        return wb.getSheetAt(sheetIndex);
    }

    private boolean alreadyExists(String word) {
        WordEntry entry = wordEntryRepository.findByWord(word);
        if (entry == null) {
            return false;
        }
        return true;
    }
}
