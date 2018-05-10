package org.oruko.dictionary.model.repository;

import org.oruko.dictionary.model.WordEntryFeedback;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import javax.transaction.Transactional;

/**
 * Repository for {@link WordEntryFeedback}
 *
 * Created by Dadepo Aderemi.
 */
@Transactional
public interface WordEntryFeedbackRepository extends CrudRepository<WordEntryFeedback, Long> {
    List<WordEntryFeedback> findByName(String name, Sort sort);
    List<WordEntryFeedback> findAll(Sort sort);

}
