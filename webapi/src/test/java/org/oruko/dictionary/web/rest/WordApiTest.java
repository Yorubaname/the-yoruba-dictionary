package org.oruko.dictionary.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.hamcrest.core.IsNot;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.oruko.dictionary.events.EventPubService;
import org.oruko.dictionary.importer.ImportStatus;
import org.oruko.dictionary.importer.ImporterInterface;
import org.oruko.dictionary.model.WordEntry;
import org.oruko.dictionary.model.State;
import org.oruko.dictionary.web.WordEntryService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test for {@link org.oruko.dictionary.web.rest.WordApi}
 */
@RunWith(MockitoJUnitRunner.class)
public class WordApiTest extends AbstractApiTest {

    @InjectMocks
    WordApi wordApi;

    @Mock
    private WordEntryService entryService;

    @Mock
    private ImporterInterface importerInterface;

    @Mock
    private EventPubService eventPubService;

    MockMvc mockMvc;
    WordEntry testWordEntry;
    WordEntry anotherTestWordEntry;

    @Before
    public void setUp() {

        LocalDateTimeDeserializer deserializer = LocalDateTimeDeserializer.INSTANCE;
        LocalDateTimeSerializer serializer = LocalDateTimeSerializer.INSTANCE;
        SimpleModule dateTimeSerializer = new SimpleModule("MyModule");
        dateTimeSerializer.addSerializer(serializer);
        dateTimeSerializer.addDeserializer(LocalDateTime.class, deserializer);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(dateTimeSerializer);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(wordApi).setMessageConverters(converter).setHandlerExceptionResolvers(
                createExceptionResolver()).build();
        testWordEntry = new WordEntry("test-entry");
        testWordEntry.setMeaning("test_meaninyig");
        anotherTestWordEntry = new WordEntry();
    }

    @Test
    public void test_get_all_words_via_get() throws Exception {
        testWordEntry.setWord("firstword");
        anotherTestWordEntry.setWord("secondword");
        when(entryService.loadAllWords()).thenReturn(Arrays.asList(testWordEntry, anotherTestWordEntry));
             mockMvc.perform(get("/v1/words?all=true"))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].word", is("firstword")))
                    .andExpect(jsonPath("$[1].word", is("secondword")))
                    .andExpect(status().isOk());
    }

    @Test
    public void test_get_all_words_filtered_by_is_submitted_by() throws Exception {
        testWordEntry.setSubmittedBy("test");
        WordEntry secondEntry = new WordEntry("secondEntry");
        secondEntry.setSubmittedBy("Not Available");
        when(entryService.loadByState(eq(Optional.empty()), any(), any())).thenReturn(Arrays.asList(testWordEntry, secondEntry));

        mockMvc.perform(get("/v1/words?submittedBy=test"))
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].word", is("test-entry")))
               .andExpect(status().isOk());
    }

    @Test
    public void test_get_all_words_filtered_by_state() throws Exception {
        testWordEntry.setState(State.PUBLISHED);
        WordEntry secondEntry = new WordEntry("secondEntry");
        secondEntry.setState(State.NEW);
        when(entryService.loadByState(eq(Optional.of(State.NEW)), any(), any())).thenReturn(Collections.singletonList(secondEntry));

        mockMvc.perform(get("/v1/words?state=NEW"))
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].word", is("secondEntry")))
               .andExpect(status().isOk());

        verify(entryService).loadByState(eq(Optional.of(State.NEW)),any(), any());
    }


    @Test
    public void test_get_a_word() throws Exception {
        when(entryService.loadWord("test-entry")).thenReturn(testWordEntry);
        mockMvc.perform(get("/v1/words/test-entry"))
               .andExpect(jsonPath("$.word", is("test-entry")))
               .andExpect(status().isOk());
    }

    @Test
    public void test_get_all_words() throws Exception {
        when(entryService.loadAllWords()).thenReturn(Arrays.asList(testWordEntry, anotherTestWordEntry));
        mockMvc.perform(get("/v1/words?all=true"))
               .andExpect(jsonPath("$").isArray())
               .andExpect(status().isOk());

        verify(entryService).loadAllWords();
    }


    @Test
    public void test_get_word_count() throws Exception {
        // Entries with NEW state
        WordEntry wordEntry_new_1 = mock(WordEntry.class);
        when(wordEntry_new_1.getState()).thenReturn(State.NEW);
        WordEntry wordEntry_new_2 = mock(WordEntry.class);
        when(wordEntry_new_2.getState()).thenReturn(State.NEW);
        WordEntry wordEntry_new_3 = mock(WordEntry.class);
        when(wordEntry_new_3.getState()).thenReturn(State.NEW);
        // Entries with modified state
        WordEntry wordEntry_modified_1 = mock(WordEntry.class);
        when(wordEntry_modified_1.getState()).thenReturn(State.MODIFIED);
        WordEntry wordEntry_modified_2 = mock(WordEntry.class);
        when(wordEntry_modified_2.getState()).thenReturn(State.MODIFIED);
        // Entries with Published State
        WordEntry wordEntry_published_2 = mock(WordEntry.class);
        when(wordEntry_published_2.getState()).thenReturn(State.PUBLISHED);

        when(entryService.loadAllWords()).thenReturn(Arrays.asList(wordEntry_new_1, wordEntry_new_2, wordEntry_new_3,
                wordEntry_modified_1, wordEntry_modified_2,
                wordEntry_published_2));


        mockMvc.perform(get("/v1/words/meta"))
               .andExpect(jsonPath("$.totalWords", is(6)))
               .andExpect(jsonPath("$.totalNewWords", is(3)))
               .andExpect(jsonPath("$.totalModifiedWords", is(2)))
               .andExpect(jsonPath("$.totalPublishedWords", is(1)))
               .andExpect(status().isOk());

    }


    @Test
    public void test_get_a_word_not_found_in_db() throws Exception {
        when(entryService.loadWord("test")).thenReturn(null);

        mockMvc.perform(get("/v1/words/test"))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void test_add_word_via_post_request() throws Exception {
        String requestJson = new ObjectMapper().writeValueAsString(testWordEntry);
        mockMvc.perform(post("/v1/words")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                                .content(requestJson))
               .andExpect(jsonPath("$.message", IsNot.not(nullValue())))
               .andExpect(status().isCreated());

        verify(entryService).insertTakingCareOfDuplicates(any(WordEntry.class));
    }

    @Test
    public void test_add_word_via_post_request_make_sure_state_is_new() throws Exception {
        testWordEntry.setState(State.NEW);
        String requestJson = new ObjectMapper().writeValueAsString(testWordEntry);
        mockMvc.perform(post("/v1/words")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                                .content(requestJson))
               .andExpect(jsonPath("$.message", IsNot.not(nullValue())))
               .andExpect(status().isCreated());

        verify(entryService).insertTakingCareOfDuplicates(any(WordEntry.class));
    }

    @Test
    public void test_add_word_via_post_request_when_state_is_null_it_will_be_new() throws Exception {
        testWordEntry.setState(null);
        ArgumentCaptor<WordEntry> argumentCaptor = ArgumentCaptor.forClass(WordEntry.class);
        String requestJson = new ObjectMapper().writeValueAsString(testWordEntry);
        mockMvc.perform(post("/v1/words")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                                .content(requestJson))
               .andExpect(jsonPath("$.message", IsNot.not(nullValue())))
               .andExpect(status().isCreated());

        verify(entryService).insertTakingCareOfDuplicates(argumentCaptor.capture());
        final WordEntry value = argumentCaptor.getValue();
        assertThat(value.getState().toString(), is("NEW"));
    }

    @Test
    public void test_add_word_via_post_request_when_state_is_not_null_expect_exception() throws Exception {
        testWordEntry.setState(State.PUBLISHED);
        String requestJson = new ObjectMapper().writeValueAsString(testWordEntry);
        mockMvc.perform(post("/v1/words")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                                .content(requestJson))
               .andExpect(jsonPath("$.message", IsNot.not(nullValue())))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void test_add_word_via_get_but_faulty_request() throws Exception {
        String requestJson = new ObjectMapper().writeValueAsString(anotherTestWordEntry);
        mockMvc.perform(post("/v1/words")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                                .content(requestJson))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void test_modifying_word_via_put_request_but_faulty_request() throws Exception {
        String requestJson = new ObjectMapper().writeValueAsString(anotherTestWordEntry);
        mockMvc.perform(put("/v1/words/jaja")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                                .content(requestJson))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void test_modifying_word_via_put_request_but_word_is_different_from_json_sent() throws Exception {
        String requestJson = new ObjectMapper().writeValueAsString(testWordEntry);
        mockMvc.perform(put("/v1/words/jaja")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                                .content(requestJson))
               .andExpect(jsonPath("$.error", is(true)))
               .andExpect(status().isInternalServerError());
    }


    @Test
    public void test_modifying_word_via_put_request_but_word_to_update_not_in_db() throws Exception {
        String requestJson = new ObjectMapper().writeValueAsString(testWordEntry);
        mockMvc.perform(put("/v1/words/test")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                                .content(requestJson))
               .andExpect(jsonPath("$.error", is(true)))
               .andExpect(status().isInternalServerError());
    }

    @Test
    public void test_modifying_word_via_put_request() throws Exception {
        WordEntry wordEntry = mock(WordEntry.class);
        when(entryService.loadWord("test")).thenReturn(wordEntry);
        String requestJson = new ObjectMapper().writeValueAsString(testWordEntry);
        mockMvc.perform(put("/v1/words/test")
                                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                                .content(requestJson))
               .andExpect(status().isCreated());

        //TODO revisit and see if argument captor can be put to use here
        verify(entryService).updateWord(isA(WordEntry.class), isA(WordEntry.class));
    }

    @Test
    public void test_uploading_vai_spreadsheet() throws Exception {

        ImportStatus importStatus = mock(ImportStatus.class);
        when(importStatus.hasErrors()).thenReturn(false);
        when(importerInterface.importFile(any())).thenReturn(importStatus);
        MockMultipartFile spreadsheet = new MockMultipartFile("wordFiles", "filename.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "some spreadsheet".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/v1/words/upload").file(spreadsheet))
               .andExpect(status().isAccepted())
               .andExpect(jsonPath("$.message", IsNot.not(nullValue())));

    }

    @Test
    public void test_batch_upload() throws Exception {
        WordEntry[] wordEntries = new WordEntry[2];
        wordEntries[0] = new WordEntry("test");
        wordEntries[1] = new WordEntry("anothertest");
        String requestJson = new ObjectMapper().writeValueAsString(wordEntries);
        mockMvc.perform(post("/v1/words/batch")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                                .content(requestJson))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.message", IsNot.not(nullValue())));

        verify(entryService, times(1)).bulkInsertTakingCareOfDuplicates(anyListOf(WordEntry.class));
    }

    @Test
    public void test_batch_upload_with_faulty_request() throws Exception {
        WordEntry[] wordEntries = new WordEntry[0];
        String requestJson = new ObjectMapper().writeValueAsString(wordEntries);
        mockMvc.perform(post("/v1/words/batch")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
                                .content(requestJson))
               .andExpect(status().isBadRequest());

        verifyZeroInteractions(entryService);
    }

    @Test
    public void test_deleting_all_words() throws Exception {
        mockMvc.perform(delete("/v1/words")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message", IsNot.not(nullValue())));

        verify(entryService).deleteAllAndDuplicates();
    }


    @Test
    public void test_get_words_with_feedback() throws Exception {
        when(entryService.loadWord("test")).thenReturn(testWordEntry);
        mockMvc.perform(get("/v1/words/{word}?feedback=true", "test"))
               .andExpect(status().isOk()).andExpect(jsonPath("$.feedback", notNullValue()));
    }

    @Test
    public void test_get_words_without_feedback() throws Exception {
        when(entryService.loadWord("test")).thenReturn(testWordEntry);
        mockMvc.perform(get("/v1/words/{word}?feedback=false", "test"))
               .andExpect(status().isOk()).andExpect(jsonPath("$.feedback").doesNotExist());
    }

}