package org.oruko.dictionary.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.oruko.dictionary.model.WordEntry;
import org.oruko.dictionary.model.WordEntryFeedback;
import org.oruko.dictionary.model.repository.WordEntryFeedbackRepository;
import org.oruko.dictionary.web.WordEntryService;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests {@link FeedbackApi}
 *
 * @author Dadepo Aderemi.
 */
@RunWith(MockitoJUnitRunner.class)
public class FeedbackApiTest extends AbstractApiTest {

    @InjectMocks
    FeedbackApi feedbackApi;

    @Mock
    WordEntryFeedbackRepository feedbackRepository;

    @Mock
    WordEntryService entryService;

    MockMvc mockMvc;

    String testName;
    String testFeedback;
    Map<String, String> feedbackMap;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(feedbackApi).setHandlerExceptionResolvers(
                createExceptionResolver()).build();

        testName =  "lagbaja";
        testFeedback = "feedback for testing";

        feedbackMap = new HashMap<>();
        feedbackMap.put("name", testName);
        feedbackMap.put("feedback", testFeedback);
    }

    @Test
    public void testGetFeedbacks() throws Exception {
        final Sort sort = new Sort(Sort.Direction.DESC, "submittedAt");

        final ArgumentCaptor<Sort> argumentCaptor = ArgumentCaptor.forClass(Sort.class);
        mockMvc.perform(get("/v1/feedbacks")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isOk());
        verify(feedbackRepository).findAll(argumentCaptor.capture());
        final Sort value = argumentCaptor.getValue();
        assertThat(value.equals(sort), is(true));
    }

    @Test
    public void testGetFeedbacksForName() throws Exception {
        final Sort sort = new Sort(Sort.Direction.DESC, "submittedAt");
        final ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        mockMvc.perform(get("/v1/feedbacks?name="+ testName)
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isOk());
        verify(feedbackRepository).findByWord(eq(testName), sortCaptor.capture());
        final Sort value = sortCaptor.getValue();
        assertThat(value.equals(sort), is(true));
    }

    @Test
    public void testdeleteAllFeedbackForName() throws Exception {
        WordEntry wordEntry = mock(WordEntry.class);
        WordEntryFeedback feedback = mock(WordEntryFeedback.class);
        when(entryService.loadWord(testName)).thenReturn(wordEntry);
        final Sort sort = new Sort(Sort.Direction.DESC, "submittedAt");
        when(feedbackRepository.findByWord(testName,sort)).thenReturn(Collections.<WordEntryFeedback>singletonList(feedback));
        final ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        mockMvc.perform(delete("/v1/feedbacks?name="+ testName)
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isOk());
        verify(feedbackRepository).findByWord(eq(testName), sortCaptor.capture());
        verify(feedbackRepository, times(1)).delete(feedback);
        final Sort value = sortCaptor.getValue();
        assertThat(value.equals(sort), is(true));
    }

    @Test
    public void testdeleteAllFeedbackForName_name_not_found() throws Exception {
        WordEntryFeedback feedback = mock(WordEntryFeedback.class);
        when(entryService.loadWord(testName)).thenReturn(null);
        mockMvc.perform(delete("/v1/feedbacks?name="+ testName)
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isBadRequest());

        verify(feedbackRepository, never()).delete(feedback);
    }

    @Test
    public void testDeleteAFeedback() throws Exception {
        WordEntryFeedback feedback = mock(WordEntryFeedback.class);
        when(feedbackRepository.findOne(1L)).thenReturn(feedback);
        mockMvc.perform(delete("/v1/feedbacks/1")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isOk());

        verify(feedbackRepository).delete(1L);
    }

    @Test
    public void testDeleteAFeedback_no_feedback_for_id() throws Exception {
        when(feedbackRepository.findOne(1L)).thenReturn(null);
        mockMvc.perform(delete("/v1/feedbacks/1")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isBadRequest());

        verify(feedbackRepository, never()).delete(1L);
    }

    @Test
    public void testDeleteAllFeedback() throws Exception {
        mockMvc.perform(delete("/v1/feedbacks")
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isOk());

        verify(feedbackRepository).deleteAll();
    }

    @Test
    public void testAddFeedback() throws Exception {
        WordEntry wordEntry = mock(WordEntry.class);
        when(entryService.loadWord(testName)).thenReturn(wordEntry);
        String requestJson = new ObjectMapper().writeValueAsString(feedbackMap);
        ArgumentCaptor<WordEntryFeedback> argumentCaptor = ArgumentCaptor.forClass(
                WordEntryFeedback.class);

        mockMvc.perform(post("/v1/feedbacks")
                                .content(requestJson)
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isCreated());

        verify(feedbackRepository).save(argumentCaptor.capture());
        final WordEntryFeedback nameEntryValue = argumentCaptor.getValue();
        assertThat(nameEntryValue.getWord(), is(testName));
        assertThat(nameEntryValue.getFeedback(), is(testFeedback));
    }

    @Test
    public void test_add_feedback_name_not_in_system() throws Exception{

        when(entryService.loadWord(testName)).thenReturn(null); // test condition
        String requestJson = new ObjectMapper().writeValueAsString(feedbackMap);

        mockMvc.perform(post("/v1/feedbacks")
                                .content(requestJson)
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error", is(true)));
    }

    @Test
    public void test_add_feedback_but_feedback_is_empty() throws Exception {

        WordEntry wordEntry = mock(WordEntry.class);
        when(entryService.loadWord(testName)).thenReturn(wordEntry);

        Map<String, String> feedbackMap = new HashMap<>();
        String testFeedback = ""; // test condition
        feedbackMap.put("feedback", testFeedback);

        String requestJson = new ObjectMapper().writeValueAsString(feedbackMap);

        mockMvc.perform(post("/v1/feedbacks")
                                .content(requestJson)
                                .contentType(MediaType.parseMediaType("application/json; charset=UTF-8")))
               .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error", is(true)));
    }

}