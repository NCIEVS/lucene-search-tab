package edu.stanford.protege.search.lucene.tab.engine;

import org.protege.editor.search.lucene.QueryEvaluationException;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 28/06/2016
 */
public interface SearchTabQuery {

    Set<OWLEntity> evaluate(SearchProgressListener listener, AtomicBoolean stopProcess) throws QueryEvaluationException;

    String getAlgebraString();

    public interface SearchProgressListener {
        
        void fireSearchingProgressed(long progress);
    }
}
