package edu.stanford.protege.search.lucene.tab.engine;

import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Collection;

/**
 * @author Josef Hardi <johardi@stanford.edu><br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/06/2016
 */
public interface SearchTabResultHandler {

    void searchFinished(Collection<OWLEntity> searchResults);
}
