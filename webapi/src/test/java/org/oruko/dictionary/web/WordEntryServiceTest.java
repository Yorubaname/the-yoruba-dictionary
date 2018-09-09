package org.oruko.dictionary.web;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.oruko.dictionary.model.WordEntry;
import org.oruko.dictionary.model.exception.RepositoryAccessError;
import org.oruko.dictionary.model.repository.WordEntryRepository;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WordEntryServiceTest {

    @Mock
    WordEntryRepository wordEntryRepository;

    // System under test
    @InjectMocks
    private WordEntryService wordEntryService;

    WordEntry wordEntry;

    WordEntry oldEntry;

    @Before
    public void setUp() {
        wordEntry = mock(WordEntry.class);
    }

    @Test
    public void testInsertTakingCareOfDuplicates_no_duplicates() throws Exception {
        String testName = "Ajani";
        when(wordEntry.getWord()).thenReturn(testName);
        when(wordEntryRepository.findByWord(testName)).thenReturn(null);
        wordEntryService.insertTakingCareOfDuplicates(wordEntry);

        verify(wordEntryRepository).save(wordEntry);
        verify(wordEntryRepository).findByWord(testName);
    }

    @Test(expected = RepositoryAccessError.class)
    public void testInsertTakingCareOfDuplicates_with_duplicates_and_name_not_in_variant() throws Exception {
        String testName = "Ajani";
        WordEntry wordEntryMock = mock(WordEntry.class);
        when(wordEntryMock.getVariants()).thenReturn(null);
        when(wordEntry.getWord()).thenReturn(testName);
        when(wordEntryRepository.findAll()).thenReturn(Collections.singletonList(wordEntryMock));
        when(wordEntryRepository.findByWord(testName)).thenReturn(wordEntry);
        wordEntryService.insertTakingCareOfDuplicates(wordEntry);

        verify(wordEntryRepository).findAll();
        verify(wordEntryRepository).findByWord(testName);
        verifyZeroInteractions(wordEntryRepository);
    }

    @Test(expected = RepositoryAccessError.class)
    public void testInsertTakingCareOfDuplicates_with_duplicates_and_name_already_in_variant() throws Exception {
        String testName = "Ajani";
        WordEntry wordEntryMock = mock(WordEntry.class);
        when(wordEntry.getWord()).thenReturn(testName);
        when(wordEntryRepository.findAll()).thenReturn(Collections.singletonList(wordEntryMock));
        when(wordEntryRepository.findByWord(testName)).thenReturn(wordEntry);
        wordEntryService.insertTakingCareOfDuplicates(wordEntry);

        verifyZeroInteractions(wordEntryRepository);
    }


    @Test
    public void testSave() throws Exception {
        wordEntryService.saveWord(wordEntry);
        verify(wordEntryRepository).save(wordEntry);
        verifyNoMoreInteractions(wordEntryRepository);
    }

    @Test
    public void testUpdate() throws Exception {
        WordEntry oldEntry = mock(WordEntry.class);
        when(oldEntry.getWord()).thenReturn("old name");
        when(wordEntryRepository.findByWord(anyString())).thenReturn(oldEntry);
        wordEntryService.updateWord(oldEntry, wordEntry);
        verify(oldEntry).update(wordEntry);
    }

    @Test
    public void testFindAll() throws Exception {
        //TODO

    }

    @Test
    public void testDeleteAllAndDuplicates() throws Exception {
        wordEntryService.deleteAllAndDuplicates();
        verify(wordEntryRepository).deleteAll();
        verifyNoMoreInteractions(wordEntryRepository);
    }

    @Test
    public void testdeleteNameEntryAndDuplicates() {
        WordEntry testName = mock(WordEntry.class);
        when(wordEntryRepository.findByWord("lagbaja")).thenReturn(testName);
        wordEntryService.deleteWordEntryAndDuplicates("lagbaja");
        verify(wordEntryRepository).delete(testName);
    }
}